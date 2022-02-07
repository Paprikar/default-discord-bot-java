package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.utils.JdaUtils.RequestErrorHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordSuggestionService.class);

    private final DiscordCategoryService categoryService;
    private final ApproveService approveService;
    private final MonitorService monitorService;

    // Map<ProviderId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    // Map<ProviderId, SuggestionChannelId>
    private final Map<Long, Long> suggestionChannels = new ConcurrentHashMap<>();

    // Map<SuggestionChannelId, ProviderId>
    private final Map<Long, Long> providers = new ConcurrentHashMap<>();

    private final RequestErrorHandler suggestionHandlingErrorHandler;

    private final RequestErrorHandler suggestionSubmittingErrorHandler;

    @Autowired
    public DiscordSuggestionService(DiscordCategoryService categoryService,
                                    ApproveService approveService,
                                    MonitorService monitorService) {
        this.categoryService = categoryService;
        this.approveService = approveService;
        this.monitorService = monitorService;

        this.suggestionHandlingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while handling the suggestion")
                .build();

        this.suggestionSubmittingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while submitting the suggestion")
                .build();
    }

    public void handleTextChannelDeleteEvent(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();

        Long providerId = providers.get(channelId);
        if (providerId == null) {
            return;
        }

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, providerId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            categories.remove(providerId);
            suggestionChannels.remove(providerId);
            providers.remove(channelId);
            monitorService.remove(monitorKey);
        }

        logger.debug("handleTextChannelDeleteEvent(): discordProvider={id={}} is disabled "
                + "due to the deletion of the required text channel with id={}", providerId, channelId);
    }

    public void handleGuildMessageReceivedEvent(@Nonnull GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        Long channelId = event.getChannel().getIdLong();

        Long providerId = providers.get(channelId);
        if (providerId == null) {
            return;
        }

        Long categoryId = categories.get(providerId);
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();

        try {
            message.delete().complete();
            handleMessageImages(message, category);
        } catch (RuntimeException e) {
            suggestionHandlingErrorHandler.accept(e);
        }
    }

    public void add(@Nonnull DiscordProviderFromDiscord provider) {
        Long categoryId = provider.getCategory().getId();
        Long providerId = provider.getId();
        Long suggestionChannelId = provider.getSuggestionChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, providerId);
        Object monitor = new Object();

        synchronized (monitor) {
            if (monitorService.putIfAbsent(monitorKey, monitor) != null) {
                return;
            }

            categories.put(providerId, categoryId);
            suggestionChannels.put(providerId, suggestionChannelId);
            providers.put(suggestionChannelId, providerId);
        }
    }

    public void remove(@Nonnull DiscordProviderFromDiscord provider) {
        Long providerId = provider.getId();
        Long suggestionChannelId = provider.getSuggestionChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, providerId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            categories.remove(providerId);
            suggestionChannels.remove(providerId);
            providers.remove(suggestionChannelId);
            monitorService.remove(monitorKey);
        }
    }

    public void update(@Nonnull DiscordProviderFromDiscord provider) {
        Long providerId = provider.getId();
        Long oldSuggestionChannelId = suggestionChannels.get(providerId);
        Long newSuggestionChannelId = provider.getSuggestionChannelId();

        if (Objects.equals(oldSuggestionChannelId, newSuggestionChannelId)) {
            return;
        }

        Object monitor = monitorService.get(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, providerId);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            suggestionChannels.put(providerId, newSuggestionChannelId);
            providers.remove(oldSuggestionChannelId);
            providers.put(newSuggestionChannelId, providerId);
        }
    }

    public boolean contains(@Nonnull DiscordProviderFromDiscord provider) {
        return categories.containsKey(provider.getId());
    }

    private void handleMessageImages(Message message, DiscordCategory category) {
        List<String> urls = new ArrayList<>();
        List<Message.Attachment> attachments = message.getAttachments();
        RestAction<PrivateChannel> privateChannel = message.getAuthor().openPrivateChannel();

        if (attachments.isEmpty()) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Suggestion Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The message must contain at least one attachment")
                    .build();

            privateChannel
                    .flatMap(channel -> channel.sendMessageEmbeds(embed)
                            .and(channel.close()))
                    .complete();
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

                privateChannel
                        .flatMap(channel -> channel.sendMessageEmbeds(embed)
                                .and(channel.close()))
                        .complete();
                return;
            }

            urls.add(attachment.getUrl());
        }

        // todo thrust list

        // todo transaction-like batch submit
        urls.forEach(url -> {
            logger.debug("handleMessagePhotos(): Submitting the suggestion with url={}", url);
            approveService.submit(category, url, suggestionSubmittingErrorHandler);
        });

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Suggestion")
                .setTimestamp(Instant.now())
                .appendDescription("Suggestion sent successfully")
                .build();

        privateChannel
                .flatMap(channel -> channel.sendMessageEmbeds(embed)
                        .and(channel.close()))
                .complete();
    }
}
