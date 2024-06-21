package dev.paprikar.defaultdiscordbot.core.session.config.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 * An interface to the configuration session commands.
 */
public interface ConfigWizardCommand {

    /**
     * Executes the command.
     *
     * @param event the event of type {@link PrivateMessageReceivedEvent} for execution
     * @param session the configuration session
     * @param argsString the arguments for command execution
     *
     * @return the state of type {@link ConfigWizardState} after execution
     */
    ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                              @Nonnull ConfigWizardSession session,
                              String argsString);

    /**
     * @return the command alias
     */
    String getName();
}
