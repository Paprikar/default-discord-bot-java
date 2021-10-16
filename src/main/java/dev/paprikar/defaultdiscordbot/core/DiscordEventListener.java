package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.core.command.DiscordCommandHandler;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
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
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class DiscordEventListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(DiscordEventListener.class);

    private final DiscordGuildService guildService;

    private final DiscordCommandHandler commandHandler;

    private final DiscordSuggestionService discordSuggestionService;

    private final ApproveService approveService;

    private final SendingService sendingService;

    private final SessionService sessionService;

    private final ReadWriteLockService readWriteLockService;

    public DiscordEventListener(DiscordGuildService guildService,
                                DiscordCommandHandler commandHandler,
                                DiscordSuggestionService discordSuggestionService,
                                ApproveService approveService,
                                SendingService sendingService,
                                SessionService sessionService,
                                ReadWriteLockService readWriteLockService) {
        this.guildService = guildService;
        this.commandHandler = commandHandler;
        this.discordSuggestionService = discordSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.sessionService = sessionService;
        this.readWriteLockService = readWriteLockService;
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

        commandHandler.handle(event);
        discordSuggestionService.handle(event);
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

        approveService.handle(event);
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

        sessionService.handle(event);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        logger.debug("onMessageReceived(): event={author={id={}}, channel={id={}}, message='{}'}",
                event.getAuthor().getIdLong(),
                event.getChannel().getIdLong(),
                event.getMessage().getContentRaw()
        );

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }
    }

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        logger.debug("onTextChannelDelete(): event={channel={id={}}",
                event.getChannel().getIdLong());

        discordSuggestionService.handle(event);
        approveService.handle(event);
        sendingService.handle(event);
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        // todo media service
        readWriteLockService.handle(event);
        setupDiscordGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        // todo media service
        readWriteLockService.handle(event);
        removeDiscordGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {
        // todo media service
        readWriteLockService.handle(event);
        setupDiscordGuild(event.getGuildIdLong());
    }

    @Override
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {
        // todo media service
        readWriteLockService.handle(event);
        removeDiscordGuild(event.getGuildIdLong());
    }

    private void setupDiscordGuild(long discordGuildId) {
        // todo default fields
        DiscordGuild guild = new DiscordGuild();
        guild.setDiscordId(discordGuildId);
        guildService.save(guild);
    }

    private void removeDiscordGuild(long discordGuildId) {
        // todo delete timeout
        guildService.deleteByDiscordId(discordGuildId);
    }
}
