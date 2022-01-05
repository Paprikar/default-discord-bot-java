package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class DiscordSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordSuggestionService.class);

    private final DiscordCategoryService categoryService;

    private final ApproveService approveService;

    private final LockService lockService;

    // Map<SuggestionChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    @Autowired
    public DiscordSuggestionService(DiscordCategoryService categoryService,
                                    ApproveService approveService,
                                    LockService lockService) {
        this.categoryService = categoryService;
        this.approveService = approveService;
        this.lockService = lockService;
    }

    private static void onSuggestionSubmitSuccess() {
        logger.debug("onSuggestionSubmitSuccess(): The suggestion was successfully submitted");
    }

    private static void onSuggestionSubmitFailure(Throwable throwable) {
        logger.warn("onSuggestionSubmitFailure(): An error occurred while submitting the suggestion", throwable);
    }

    public void handleTextChannelDeleteEvent(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();
        Long categoryId = categories.get(channelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(channelId);

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void handleGuildMessageReceivedEvent(@Nonnull GuildMessageReceivedEvent event) {
        Message message = event.getMessage();

        Long categoryId = categories.get(event.getChannel().getIdLong());
        if (categoryId == null) {
            return;
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();

        message.delete().queue();

        handleMessageImages(message, category);
    }

    public void add(Long categoryId, Long suggestionChannelId) {
        Lock lock = new ReentrantLock();
        lock.lock();
        // todo per provider lock
        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, categoryId);
        if (lockService.putIfAbsent(lockKey, lock) != null) {
            lock.unlock();
            return;
        }

        categories.put(suggestionChannelId, categoryId);

        lock.unlock();
    }

    public void remove(Long suggestionChannelId) {
        Long categoryId = categories.get(suggestionChannelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(suggestionChannelId);

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void update(Long oldSuggestionChannelId, Long newSuggestionChannelId) {
        Long categoryId = categories.get(oldSuggestionChannelId);
        Lock lock = lockService.get(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, categoryId);
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

    private void handleMessageImages(Message message, DiscordCategory category) {
        List<String> urls = new ArrayList<>();
        List<Message.Attachment> attachments = message.getAttachments();

        if (attachments.isEmpty()) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Suggestion Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The message must contain at least one attachment")
                    .build();

            message.getAuthor().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessageEmbeds(embed))
                    .queue();

            return;
        }

        for (Message.Attachment attachment : attachments) {
            if (!attachment.isImage()) {
                MessageEmbed embed = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Suggestion Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("The attachments of the message must be images only")
                        .build();

                message.getAuthor().openPrivateChannel()
                        .flatMap(channel -> channel.sendMessageEmbeds(embed))
                        .queue();

                return;
            }

            urls.add(attachment.getUrl());
        }

        // todo thrust list

        // todo transaction-like batch submit
        for (String url : urls) {
            logger.debug("handleMessageImages(): Submitting the suggestion with url={}", url);
            approveService.submit(category, url, (Supplier<? extends RestAction<Void>>) null,
                    DiscordSuggestionService::onSuggestionSubmitSuccess,
                    DiscordSuggestionService::onSuggestionSubmitFailure);
        }

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Suggestion")
                .setTimestamp(Instant.now())
                .appendDescription("Suggestion sent successfully")
                .build();

        message.getAuthor().openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed))
                .queue();
    }
}
