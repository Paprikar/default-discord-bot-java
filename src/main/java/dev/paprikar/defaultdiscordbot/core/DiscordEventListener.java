package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.config.DdbDefaults;
import dev.paprikar.defaultdiscordbot.core.command.DiscordCommandHandler;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.SessionService;
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

@Component
public class DiscordEventListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DiscordEventListener.class);

    private final DiscordGuildService guildService;
    private final DiscordCategoryService categoryService;
    private final DiscordMediaRequestService mediaRequestService;
    private final DiscordProviderFromDiscordService discordProviderService;
    private final DiscordCommandHandler commandHandler;
    private final DiscordSuggestionService discordSuggestionService;
    private final ApproveService approveService;
    private final SendingService sendingService;
    private final MediaActionService mediaActionService;
    private final SessionService sessionService;
    private final DdbConfig config;

    @Autowired
    public DiscordEventListener(DiscordGuildService guildService,
                                DiscordCategoryService categoryService,
                                DiscordMediaRequestService mediaRequestService,
                                DiscordProviderFromDiscordService discordProviderService,
                                DiscordCommandHandler commandHandler,
                                DiscordSuggestionService discordSuggestionService,
                                ApproveService approveService,
                                SendingService sendingService,
                                MediaActionService mediaActionService,
                                SessionService sessionService,
                                DdbConfig config) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.discordProviderService = discordProviderService;
        this.commandHandler = commandHandler;
        this.discordSuggestionService = discordSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.mediaActionService = mediaActionService;
        this.sessionService = sessionService;
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

        sessionService.handlePrivateMessageReceivedEvent(event);
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
            discordProviderService.deleteAllByCategoryId(categoryId);
        });

        guildService.deleteByDiscordId(guildDiscordId);
    }
}
