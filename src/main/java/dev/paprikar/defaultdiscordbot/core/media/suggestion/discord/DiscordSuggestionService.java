package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.FileExtension;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Service
public class DiscordSuggestionService {

    private final Logger logger = LoggerFactory.getLogger(DiscordSuggestionService.class);

    private final DiscordGuildService guildService;

    private final DiscordCategoryService categoryService;

    private final ReadWriteLockService readWriteLockService;

    // Map<SuggestionChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    public DiscordSuggestionService(
            DiscordGuildService guildService,
            DiscordCategoryService categoryService,
            ReadWriteLockService readWriteLockService) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.readWriteLockService = readWriteLockService;
    }

    public void handle(@Nonnull TextChannelDeleteEvent event) {
        Optional<DiscordGuild> guildOptional = guildService.findByDiscordId(event.getGuild().getIdLong());
        if (!guildOptional.isPresent()) {
            return;
        }

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, guildOptional.get().getId());
        if (lock == null) {
            return;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        categories.remove(event.getChannel().getIdLong());

        writeLock.unlock();
    }

    public void handle(@Nonnull GuildMessageReceivedEvent event) {
        Optional<DiscordGuild> guildOptional = guildService.findByDiscordId(event.getGuild().getIdLong());
        if (!guildOptional.isPresent()) {
            return;
        }

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, guildOptional.get().getId());
        if (lock == null) {
            return;
        }

        Lock readLock = lock.readLock();
        readLock.lock();

        Long categoryId = categories.get(event.getChannel().getIdLong());
        if (categoryId == null) {
            readLock.unlock();
            return;
        }

        // todo get inside the lock
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            readLock.unlock();
            return;
        }
        DiscordCategory category = categoryOptional.get();

        Message eventMessage = event.getMessage();
        String link = eventMessage.getContentRaw();
        if (link.isEmpty()) { // from attachments
            List<Message.Attachment> attachments = eventMessage.getAttachments();
            if (attachments.isEmpty()) {
                // todo invalid input response
                readLock.unlock();
                return;
            }
            if (attachments.size() != 1) {
                // todo invalid input response
                readLock.unlock();
                return;
            }
            Message.Attachment attachment = attachments.get(0);
            if (!(attachment.isImage() || attachment.isVideo())) {
                // todo invalid input response
                readLock.unlock();
                return;
            }
            link = attachment.getUrl();
        } else { // from content
            if (link.matches(".*?(\\R|\\s).*")) {
                // todo invalid input response
                readLock.unlock();
                return;
            }
//            if (!isValidExtension(link)) { // todo fix validation ?
//                // todo invalid input response
//                readLock.unlock();
//                return;
//            }
        }

        // todo thrust list | saving bypassing the approval with readLock

        TextChannel approvalChannel = event.getJDA().getTextChannelById(category.getApprovalChannelId());
        if (approvalChannel == null) {
            // todo failure callback (to suggestion channel)
            readLock.unlock();
            return;
        }

        // todo add suggester info
        approvalChannel.sendMessage(link)
                .flatMap(message -> message
                        .addReaction(category.getPositiveApprovalEmoji().toString())
                        .and(message.addReaction(category.getNegativeApprovalEmoji().toString())))
                .flatMap(unused -> eventMessage.delete())
                .queue(unused -> onSuggestionSendingSuccess(readLock),
                        throwable -> onSuggestionSendingFailure(throwable, readLock));
    }

    public void addSuggestionChannel(Long channelId, Long categoryId) {
        categories.put(channelId, categoryId);
    }

    public void removeSuggestionChannel(Long channelId) {
        categories.remove(channelId);
    }

    public void updateSuggestionChannel(Long oldChannelId, Long newChannelId) {
        Long categoryId = categories.get(oldChannelId);
        categories.put(newChannelId, categoryId);
        categories.remove(oldChannelId);
    }

    private void onSuggestionSendingSuccess(Lock lock) {
        // todo success callback (to suggestion channel)
        lock.unlock();
    }

    private void onSuggestionSendingFailure(Throwable throwable, Lock lock) {
        logger.error("An error occurred while sending the suggestion", throwable);
        // todo failure callback (to suggestion channel)
        lock.unlock();
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
