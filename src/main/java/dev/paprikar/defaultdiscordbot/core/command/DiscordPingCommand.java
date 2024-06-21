package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The command for testing this bot to see if it works.
 */
@Component
public class DiscordPingCommand implements DiscordCommand {

    private static final String NAME = "ping";

    private final RequestErrorHandler executionErrorHandler;

    /**
     * Constructs the command.
     */
    @Autowired
    public DiscordPingCommand() {
        this.executionErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while sending the ping response")
                .build();
    }

    @Override
    public void execute(@Nonnull GuildMessageReceivedEvent event, @Nonnull String argsString) {
        event.getChannel()
                .sendMessage("...pong")
                .reference(event.getMessage())
                .queue(null, executionErrorHandler);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
