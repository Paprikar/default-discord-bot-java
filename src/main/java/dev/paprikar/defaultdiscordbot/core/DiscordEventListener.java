package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.config.DdbDefaults;
import dev.paprikar.defaultdiscordbot.core.command.DiscordCommandHandler;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSessionService;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * The component that defines the event listener of the discord bot.
 */
@Component
public class DiscordEventListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DiscordEventListener.class);

    private final DiscordGuildService guildService;
    private final DiscordCategoryService categoryService;
    private final DiscordMediaRequestService mediaRequestService;
    private final DiscordProviderFromDiscordService discordProviderService;
    private final DiscordProviderFromVkService vkProviderService;
    private final DiscordCommandHandler commandHandler;
    private final DiscordSuggestionService discordSuggestionService;
    private final ApproveService approveService;
    private final SendingService sendingService;
    private final MediaActionService mediaActionService;
    private final ConfigWizardSessionService configWizardSessionService;
    private final DdbConfig config;

    /**
     * Constructs the component.
     *
     * @param guildService
     *         an instance of {@link DiscordGuildService}
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param mediaRequestService
     *         an instance of {@link DiscordMediaRequestService}
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param commandHandler
     *         an instance of {@link DiscordCommandHandler}
     * @param discordSuggestionService
     *         an instance of {@link DiscordSuggestionService}
     * @param approveService
     *         an instance of {@link ApproveService}
     * @param sendingService
     *         an instance of {@link SendingService}
     * @param mediaActionService
     *         an instance of {@link MediaActionService}
     * @param configWizardSessionService
     *         an instance of {@link ConfigWizardSessionService}
     * @param config
     *         an instance of {@link DdbConfig}
     */
    @Autowired
    public DiscordEventListener(DiscordGuildService guildService,
                                DiscordCategoryService categoryService,
                                DiscordMediaRequestService mediaRequestService,
                                DiscordProviderFromDiscordService discordProviderService,
                                DiscordProviderFromVkService vkProviderService,
                                DiscordCommandHandler commandHandler,
                                DiscordSuggestionService discordSuggestionService,
                                ApproveService approveService,
                                SendingService sendingService,
                                MediaActionService mediaActionService,
                                ConfigWizardSessionService configWizardSessionService,
                                DdbConfig config) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.discordProviderService = discordProviderService;
        this.vkProviderService = vkProviderService;
        this.commandHandler = commandHandler;
        this.discordSuggestionService = discordSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.mediaActionService = mediaActionService;
        this.configWizardSessionService = configWizardSessionService;
        this.config = config;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        logger.debug("onReady(): event={guildAvailableCount={}, guildUnavailableCount={}}",
                event.getGuildAvailableCount(), event.getGuildUnavailableCount());
    }

    @Override
    public void onResumed(@Nonnull ResumedEvent event) {
        logger.debug("onResumed()");
    }

    @Override
    public void onReconnected(@Nonnull ReconnectedEvent event) {
        logger.debug("onReconnected()");
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        logger.debug("onDisconnect(): event={timeDisconnected={}, closeCode={}}",
                event.getTimeDisconnected(), event.getCloseCode());
    }

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        logger.debug("onShutdown(): event={shutdownTime={}, closeCode={}}",
                event.getTimeShutdown(), event.getCloseCode());
    }

    @Override
    public void onStatusChange(@Nonnull StatusChangeEvent event) {
        logger.debug("onStatusChange(): event={}", event);
    }

    @Override
    public void onException(@Nonnull ExceptionEvent event) {
        logger.debug("onException(): cause='{}'", event.getCause().toString());
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        logger.debug("onGuildMessageReceived(): event={guild={id={}}, author={id={}}, channel={id={}}, message='{}'}",
                event.getGuild().getIdLong(),
                event.getAuthor().getIdLong(),
                event.getChannel().getIdLong(),
                event.getMessage().getContentRaw()
        );

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        commandHandler.handleGuildMessageReceivedEvent(event);
        discordSuggestionService.handleGuildMessageReceivedEvent(event);
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        logger.debug("onGuildMessageReactionAdd(): event=" +
                        "{guild={id={}}, user={id={}}, channel={id={}}, message={id={}}}",
                event.getGuild().getIdLong(),
                event.getUserId(),
                event.getChannel().getIdLong(),
                event.getMessageIdLong()
        );

        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }

        approveService.handleGuildMessageReactionAddEvent(event);
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        logger.debug("onPrivateMessageReceived(): event={author={id={}}, channel={id={}}, message='{}'}",
                event.getAuthor().getIdLong(),
                event.getChannel().getIdLong(),
                event.getMessage().getContentRaw()
        );

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        configWizardSessionService.handlePrivateMessageReceivedEvent(event);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        logger.debug("onMessageReceived(): event={author={id={}}, channel={id={}}, message='{}'}",
                event.getAuthor().getIdLong(),
                event.getChannel().getIdLong(),
                event.getMessage().getContentRaw()
        );
    }

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        logger.debug("onTextChannelDelete(): event={channel={id={}}",
                event.getChannel().getIdLong());

        discordSuggestionService.handleTextChannelDeleteEvent(event);
        approveService.handleTextChannelDeleteEvent(event);
        sendingService.handleTextChannelDeleteEvent(event);
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        setupDiscordGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        removeDiscordGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {
        setupDiscordGuild(event.getGuildIdLong());
    }

    @Override
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {
        removeDiscordGuild(event.getGuildIdLong());
    }

    private void setupDiscordGuild(long guildDiscordId) {
        DiscordGuild guild = new DiscordGuild();
        DdbDefaults defaults = config.getDefaults();

        guild.setDiscordId(guildDiscordId);
        guild.setPrefix(defaults.getPrefix());

        guildService.save(guild);
    }

    private void removeDiscordGuild(long guildDiscordId) {
        // todo delete timeout

        categoryService.findAllByGuildDiscordId(guildDiscordId).forEach(category -> {
            mediaActionService.disableCategory(category);
            Long categoryId = category.getId();
            categoryService.deleteById(categoryId);
            mediaRequestService.deleteByCategoryId(categoryId);
            discordProviderService.deleteByCategoryId(categoryId);
            vkProviderService.deleteByCategoryId(categoryId);
        });

        guildService.deleteByDiscordId(guildDiscordId);
    }
}
