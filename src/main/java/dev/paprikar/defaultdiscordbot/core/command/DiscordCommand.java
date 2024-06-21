package dev.paprikar.defaultdiscordbot.core.command;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The interface for Discord commands.
 */
public interface DiscordCommand {

    /**
     * Executes the command.
     *
     * @param event the event of type {@link GuildMessageReceivedEvent} for execution
     * @param argsString the arguments for command execution
     */
    void execute(@Nonnull GuildMessageReceivedEvent event, @Nonnull String argsString);

    /**
     * @return the command alias
     */
    String getName();
}
