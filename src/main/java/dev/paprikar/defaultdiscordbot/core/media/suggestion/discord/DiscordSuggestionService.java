package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.FileExtension;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DiscordSuggestionService {

    private final Logger logger = LoggerFactory.getLogger(DiscordSuggestionService.class);

    private final DiscordCategoryService categoryService;

    private final LockService lockService;

    // Map<SuggestionChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    @Autowired
    public DiscordSuggestionService(DiscordCategoryService categoryService, LockService lockService) {
        this.categoryService = categoryService;
        this.lockService = lockService;
    }

    public void handle(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();
        Long categoryId = categories.get(channelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_DISCORD_PROVIDER_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(channelId);

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void handle(@Nonnull GuildMessageReceivedEvent event) {
        Long categoryId = categories.get(event.getChannel().getIdLong());
        if (categoryId == null) {
            return;
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();

        Message eventMessage = event.getMessage();
        List<Message.Attachment> attachments = eventMessage.getAttachments();
        String url;

        if (!attachments.isEmpty()) { // from attachments
            if (attachments.size() != 1) {
                // todo invalid input response
                return;
            }
            Message.Attachment attachment = attachments.get(0);

            if (!(attachment.isImage() || attachment.isVideo())) {
                // todo invalid input response
                return;
            }

            url = attachment.getUrl();
        } else { // from content
            url = eventMessage.getContentRaw();
            if (url.matches(".*?(\\R|\\s).*")) { // only one request
                // todo invalid input response
                return;
            }

//            if (!isValidExtension(url)) { // todo fix validation ?
//                // todo invalid input response
//                return;
//            }
        }

        // todo thrust list

        TextChannel approvalChannel = event.getJDA().getTextChannelById(category.getApprovalChannelId());
        if (approvalChannel == null) {
            // todo failure callback (to suggestion channel)
            return;
        }

        // todo add suggester info
        approvalChannel.sendMessage(url)
                .flatMap(message -> message
                        .addReaction(category.getPositiveApprovalEmoji().toString())
                        .and(message.addReaction(category.getNegativeApprovalEmoji().toString())))
                .flatMap(unused -> eventMessage.delete())
                .queue(unused -> onSuggestionSendingSuccess(), this::onSuggestionSendingFailure);
    }

    public void addCategory(Long categoryId, Long suggestionChannelId) {
        Lock lock = new ReentrantLock();
        lock.lock();
        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_DISCORD_PROVIDER_CONFIGURATION, categoryId);
        if (lockService.putIfAbsent(lockKey, lock) != null) {
            lock.unlock();
            return;
        }

        categories.put(suggestionChannelId, categoryId);

        lock.unlock();
    }

    public void removeCategory(Long suggestionChannelId) {
        Long categoryId = categories.get(suggestionChannelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_DISCORD_PROVIDER_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(suggestionChannelId);

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void updateCategory(Long oldSuggestionChannelId, Long newSuggestionChannelId) {
        Long categoryId = categories.get(oldSuggestionChannelId);
        Lock lock = lockService.get(ConcurrencyScope.CATEGORY_DISCORD_PROVIDER_CONFIGURATION, categoryId);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.put(newSuggestionChannelId, categoryId);
        if (!oldSuggestionChannelId.equals(newSuggestionChannelId)) {
            categories.remove(oldSuggestionChannelId);
        }

        lock.unlock();
    }

    private void onSuggestionSendingSuccess() {
        // todo success callback (to suggestion channel)
    }

    private void onSuggestionSendingFailure(Throwable throwable) {
        logger.error("An error occurred while sending the suggestion", throwable);
        // todo failure callback (to suggestion channel)
    }

    private boolean isValidExtension(String fileName) {
        String extension = getFileExtension(fileName);
        return extension != null && FileExtension.EXTENSIONS.contains(extension.toLowerCase());
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.') + 1;
        return index == 0 || index == fileName.length() ? null : fileName.substring(index);
    }
}
