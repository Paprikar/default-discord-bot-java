package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.session.SessionService;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class DiscordConfigCommand implements DiscordCommand {

    private static final String NAME = "config";

    private final SessionService sessionService;

    @Autowired
    public DiscordConfigCommand(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void execute(@Nonnull String argsString, @Nonnull GuildMessageReceivedEvent event) {
        sessionService.handleGuildMessageReceivedEvent(event);
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
