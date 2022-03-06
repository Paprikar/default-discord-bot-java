package dev.paprikar.defaultdiscordbot.core.session.connections.state.vk.command;

import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * The command for switching from vk directory to initial directory.
 */
@Component
public class ConnectionsWizardVkBackCommand implements ConnectionsWizardVkCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardVkBackCommand.class);

    private static final String NAME = "back";

    /**
     * Constructs the command.
     */
    @Autowired
    public ConnectionsWizardVkBackCommand() {
    }

    @Override
    public ConnectionsWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                          @Nonnull ConnectionsWizardSession session,
                                          String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        return ConnectionsWizardState.ROOT;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
