package dev.paprikar.defaultdiscordbot.core.session.connections.command;

import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 * An interface to the connections session commands.
 */
public interface ConnectionsWizardCommand {

    /**
     * Executes the command.
     *
     * @param event the event of type {@link PrivateMessageReceivedEvent} for execution
     * @param session the connections session
     * @param argsString the arguments for command execution
     *
     * @return the state of type {@link ConnectionsWizardState} after execution
     */
    ConnectionsWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                   @Nonnull ConnectionsWizardSession session,
                                   String argsString);

    /**
     * @return the command alias
     */
    String getName();
}
