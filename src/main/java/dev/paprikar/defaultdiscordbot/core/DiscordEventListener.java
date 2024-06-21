package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.core.command.DiscordCommandHandler;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSessionService;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSessionService;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
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

/**
 * The component that defines the event listener of the discord bot.
 */
@Component
public class DiscordEventListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DiscordEventListener.class);

    private final DiscordBotService discordBotService;
    private final DiscordCommandHandler commandHandler;
    private final DiscordSuggestionService discordSuggestionService;
    private final ApproveService approveService;
    private final SendingService sendingService;
    private final ConfigWizardSessionService configWizardSessionService;
    private final ConnectionsWizardSessionService connectionsWizardSessionService;

    /**
     * Constructs the component.
     *
     * @param discordBotService an instance of {@link DiscordBotService}
     * @param commandHandler an instance of {@link DiscordCommandHandler}
     * @param discordSuggestionService an instance of {@link DiscordSuggestionService}
     * @param approveService an instance of {@link ApproveService}
     * @param sendingService an instance of {@link SendingService}
     * @param configWizardSessionService an instance of {@link ConfigWizardSessionService}
     * @param connectionsWizardSessionService an instance of {@link ConnectionsWizardSessionService}
     */
    @Autowired
    public DiscordEventListener(DiscordBotService discordBotService,
                                DiscordCommandHandler commandHandler,
                                DiscordSuggestionService discordSuggestionService,
                                ApproveService approveService,
                                SendingService sendingService,
                                ConfigWizardSessionService configWizardSessionService,
                                ConnectionsWizardSessionService connectionsWizardSessionService) {
        this.discordBotService = discordBotService;
        this.commandHandler = commandHandler;
        this.discordSuggestionService = discordSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.configWizardSessionService = configWizardSessionService;
        this.connectionsWizardSessionService = connectionsWizardSessionService;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        logger.debug("onReady(): event={guildAvailableCount={}, guildUnavailableCount={}}",
                event.getGuildAvailableCount(), event.getGuildUnavailableCount());
    }

    @Override
    public void onResumed(@Nonnull ResumedEvent event) {
        logger.debug("onResumed()");

        discordBotService.initialize(event.getJDA());
    }

    @Override
    public void onReconnected(@Nonnull ReconnectedEvent event) {
        logger.debug("onReconnected()");

        discordBotService.initialize(event.getJDA());
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        logger.debug("onDisconnect(): event={timeDisconnected={}, closeCode={}}",
                event.getTimeDisconnected(), event.getCloseCode());

        discordBotService.shutdown();
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
        connectionsWizardSessionService.handlePrivateMessageReceivedEvent(event);
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
        long guildId = event.getGuild().getIdLong();
        logger.debug("onGuildJoin(): guild={id={}}", guildId);

        discordBotService.setupDiscordGuild(guildId);
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        logger.debug("onGuildLeave(): guild={id={}}", guildId);

        discordBotService.deleteDiscordGuild(guildId);
    }

    @Override
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {
        long guildId = event.getGuildIdLong();
        logger.debug("onUnavailableGuildJoined(): guild={id={}}", guildId);

        discordBotService.setupDiscordGuild(guildId);
    }

    @Override
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {
        long guildId = event.getGuildIdLong();
        logger.debug("onUnavailableGuildLeave(): guild={id={}}", guildId);

        discordBotService.deleteDiscordGuild(guildId);
    }
}
