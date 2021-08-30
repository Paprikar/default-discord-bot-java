package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.core.command.DiscordCommandHandler;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.SessionService;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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

    private final SessionService sessionService;

    public DiscordEventListener(DiscordGuildService guildService,
                                DiscordCommandHandler commandHandler,
                                SessionService sessionService) {
        this.guildService = guildService;
        this.commandHandler = commandHandler;
        this.sessionService = sessionService;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        logger.debug("onGuildMessageReceived(GuildMessageReceivedEvent event): " +
                        "event={guildId={}, author={}, channel={}, message='{}'}",
                event.getGuild().getIdLong(),
                event.getAuthor(),
                event.getChannel(),
                event.getMessage().getContentRaw()
        );

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        commandHandler.handle(event);
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        logger.debug("onPrivateMessageReceived(PrivateMessageReceivedEvent event): " +
                        "event={author={}, channel={}, message='{}'}",
                event.getAuthor(),
                event.getChannel(),
                event.getMessage().getContentRaw()
        );

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        sessionService.handlePrivateMessage(event);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        logger.debug("onMessageReceived(MessageReceivedEvent event): " +
                        "event={author={}, channel={}, message='{}'}",
                event.getAuthor(),
                event.getChannel(),
                event.getMessage().getContentRaw()
        );

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }
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

    private void setupDiscordGuild(long discordGuildId) {
        // todo default fields
        DiscordGuild guild = new DiscordGuild();
        guild.setDiscordId(discordGuildId);
        guildService.saveGuild(guild);
    }

    private void removeDiscordGuild(long discordGuildId) {
        // todo delete timeout
        guildService.deleteGuildByDiscordId(discordGuildId);
    }
}
