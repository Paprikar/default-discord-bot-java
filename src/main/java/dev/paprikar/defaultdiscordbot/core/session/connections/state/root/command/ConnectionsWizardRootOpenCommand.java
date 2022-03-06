package dev.paprikar.defaultdiscordbot.core.session.connections.state.root.command;

import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * The command for switching to the directory in the initial directory.
 */
@Component
public class ConnectionsWizardRootOpenCommand implements ConnectionsWizardRootCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardRootOpenCommand.class);

    private static final String NAME = "open";

    // Map<Directory, State>
    private final Map<String, ConnectionsWizardState> targets = new HashMap<>();

    /**
     * Constructs the command.
     */
    @Autowired
    public ConnectionsWizardRootOpenCommand() {
        setupTargets();
    }

    @Override
    public ConnectionsWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                          @Nonnull ConnectionsWizardSession session,
                                          String argsString) {
        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing for privateSession={}", session);
            return ConnectionsWizardState.IGNORE;
        }

        ConnectionsWizardState targetState = targets.get(argsString);
        if (targetState == null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Connections Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The directory with the name `" + argsString + "` does not exist")
                    .build()
            );

            return ConnectionsWizardState.KEEP;
        }

        logger.debug("Open at ROOT: privateSession={}, target='{}'", session, argsString);

        return targetState;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void setupTargets() {
        targets.put("vk", ConnectionsWizardState.VK);
    }
}
