package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.session.SessionService;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

public class DiscordConfigCommand implements DiscordCommand {

    private final SessionService sessionService;

    public DiscordConfigCommand(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void execute(@Nonnull String argsString, @Nonnull GuildMessageReceivedEvent event) {
        sessionService.handle(event);
    }
}
