package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
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

/**
 * Service for discord suggestions.
 */
@Service
public class DiscordSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordSuggestionService.class);

    private final DiscordProviderFromDiscordService discordProviderService;
    private final DiscordTrustedSuggesterService trustedSuggesterService;
    private final ApproveService approveService;
    private final SendingService sendingService;
    private final MonitorService monitorService;

    // Map<ProviderId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    // Map<ProviderId, SuggestionChannelId>
    private final Map<Long, Long> suggestionChannels = new ConcurrentHashMap<>();

    // Map<SuggestionChannelId, ProviderId>
    private final Map<Long, Long> providers = new ConcurrentHashMap<>();

    private final RequestErrorHandler suggestionHandlingErrorHandler;

    private final RequestErrorHandler suggestionSubmittingErrorHandler;

    /**
     * Constructs the service.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     * @param trustedSuggesterService
     *         an instance of {@link DiscordTrustedSuggesterService}
     * @param approveService
     *         an instance of {@link ApproveService}
     * @param sendingService
     *         an instance of {@link SendingService}
     * @param monitorService
     *         an instance of {@link MonitorService}
     */
    @Autowired
    public DiscordSuggestionService(DiscordProviderFromDiscordService discordProviderService,
                                    DiscordTrustedSuggesterService trustedSuggesterService,
                                    ApproveService approveService,
                                    SendingService sendingService,
                                    MonitorService monitorService) {
        this.discordProviderService = discordProviderService;
        this.trustedSuggesterService = trustedSuggesterService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.monitorService = monitorService;

        this.suggestionHandlingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while handling the suggestion")
                .build();

        this.suggestionSubmittingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while submitting the suggestion")
                .build();
    }

    /**
     * Handles events of type {@link TextChannelDeleteEvent}.
     *
     * @param event
     *         the event of type {@link TextChannelDeleteEvent} for handling
     */
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

    /**
     * Handles events of type {@link GuildMessageReceivedEvent}.
     *
     * @param event
     *         the event of type {@link GuildMessageReceivedEvent} for handling
     */
    public void handleGuildMessageReceivedEvent(@Nonnull GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        Long channelId = event.getChannel().getIdLong();

        Long providerId = providers.get(channelId);
        if (providerId == null) {
            return;
        }

        Optional<DiscordProviderFromDiscord> providerOptional = discordProviderService.findById(providerId);
        if (providerOptional.isEmpty()) {
            return;
        }
        DiscordProviderFromDiscord provider = providerOptional.get();

        try {
            message.delete().complete();
            handleMessageImages(message, provider);
        } catch (RuntimeException e) {
            suggestionHandlingErrorHandler.accept(e);
        }
    }

    /**
     * Adds the discord provider to suggestion processing context.
     *
     * @param provider
     *         the discord provider
     */
    public void add(@Nonnull DiscordProviderFromDiscord provider) {
        Long categoryId = provider.getCategory().getId();
        Long providerId = provider.getId();
        Long suggestionChannelId = provider.getSuggestionChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, providerId);
        Object monitor = new Object();

        synchronized (monitor) {
            if (monitorService.putIfAbsent(monitorKey, monitor) != null) {
                logger.debug("add(): Provider is already added. Skipping");
                return;
            }

            categories.put(providerId, categoryId);
            suggestionChannels.put(providerId, suggestionChannelId);
            providers.put(suggestionChannelId, providerId);
        }
    }

    /**
     * Removes the discord provider from suggestion processing context.
     *
     * @param provider
     *         the discord provider
     */
    public void remove(@Nonnull DiscordProviderFromDiscord provider) {
        Long providerId = provider.getId();
        Long suggestionChannelId = provider.getSuggestionChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION, providerId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            logger.debug("remove(): Provider is already removed. Skipping");
            return;
        }

        synchronized (monitor) {
            categories.remove(providerId);
            suggestionChannels.remove(providerId);
            providers.remove(suggestionChannelId);
            monitorService.remove(monitorKey);
        }
    }

    /**
     * Updates the discord provider in suggestion processing context.
     *
     * @param provider
     *         the discord provider
     */
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

    /**
     * Does the discord provider exists in suggestion processing context?
     *
     * @param provider
     *         the discord provider
     *
     * @return {@code true} if the discord provider exists in suggestion processing context
     */
    public boolean contains(@Nonnull DiscordProviderFromDiscord provider) {
        return categories.containsKey(provider.getId());
    }

    private void handleMessageImages(Message message, DiscordProviderFromDiscord provider) {
        List<String> urls = new ArrayList<>();
        List<Message.Attachment> attachments = message.getAttachments();
        User author = message.getAuthor();
        RestAction<PrivateChannel> privateChannel = author.openPrivateChannel();

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

        DiscordCategory category = provider.getCategory();
        boolean isTrusted = trustedSuggesterService.existsByCategoryIdAndUserId(category.getId(), author.getIdLong());
        boolean isBulkSubmit = category.isBulkSubmit();
        boolean isSendingSubmit = isTrusted && (urls.size() == 1 || isBulkSubmit);

        // todo transaction-like batch submit
        urls.forEach(url -> {
            if (isSendingSubmit) {
                sendingService.submit(category, url);
            } else {
                MessageEmbed suggestion = new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTimestamp(Instant.now())
                        .appendDescription("Provider type: `Discord`\n")
                        .appendDescription(String.format("Provider name: `%s`\n", provider.getName()))
                        .appendDescription(String.format("Author: %s", author.getAsMention()))
                        .setImage(url)
                        .build();

                logger.debug("handleMessagePhotos(): Submitting the suggestion: "
                                + "provider={id={}}, author={}, url={}, suggestion={timestamp={}}",
                        provider.getId(), author.getAsTag(), url, suggestion.getTimestamp());

                approveService.submit(category, suggestion, suggestionSubmittingErrorHandler);
            }
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
