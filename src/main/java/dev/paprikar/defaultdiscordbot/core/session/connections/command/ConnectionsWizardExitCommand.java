package dev.paprikar.defaultdiscordbot.core.session.connections.command;

import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

/**
 * The command for ending a connections session.
 */
public class ConnectionsWizardExitCommand implements ConnectionsWizardCommand {

    private static final String NAME = "exit";

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardExitCommand.class);

    public ConnectionsWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                          @Nonnull ConnectionsWizardSession session,
                                          String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        session.getResponses().add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Connections Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Connections session is finished")
                .build()
        );

        return ConnectionsWizardState.END;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
