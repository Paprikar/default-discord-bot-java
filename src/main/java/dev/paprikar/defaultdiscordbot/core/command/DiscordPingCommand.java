package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.utils.JdaUtils.RequestErrorHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class DiscordPingCommand implements DiscordCommand {

    private static final String NAME = "ping";

    private final RequestErrorHandler executionErrorHandler;

    @Autowired
    public DiscordPingCommand() {
        this.executionErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while sending the ping response")
                .build();
    }

    @Override
    public void execute(@Nonnull String argsString, @Nonnull GuildMessageReceivedEvent event) {
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
