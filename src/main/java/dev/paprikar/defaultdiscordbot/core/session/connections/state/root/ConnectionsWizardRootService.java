package dev.paprikar.defaultdiscordbot.core.session.connections.state.root;

import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizard;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import dev.paprikar.defaultdiscordbot.core.session.connections.state.root.command.ConnectionsWizardRootCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for the connections state of initial directory.
 */
@Service
public class ConnectionsWizardRootService extends ConnectionsWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardRootService.class);

    /**
     * Constructs a connections state service.
     *
     * @param commands
     *         a {@link List} of instances of {@link ConnectionsWizardRootCommand}
     */
    @Autowired
    public ConnectionsWizardRootService(List<ConnectionsWizardRootCommand> commands) {
        super();

        commands.forEach(command -> this.commands.put(command.getName(), command));
    }

    @Transactional
    @Override
    public ConnectionsWizardState handle(@Nonnull PrivateMessageReceivedEvent event,
                                         @Nonnull ConnectionsWizardSession session) {
        logger.trace("handle(): privateSession={}", session);

        return super.handle(event, session);
    }

    @Transactional
    @Override
    public void print(@Nonnull ConnectionsWizardSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            MessageEmbed embed = getDescription();
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel()
                    .sendMessageEmbeds(responses)
                    .queue(null, printingErrorHandler);
            session.setResponses(new ArrayList<>());
        }
    }

    @Override
    public ConnectionsWizardState getState() {
        return ConnectionsWizardState.ROOT;
    }

    private MessageEmbed getDescription() {
        EmbedBuilder builder = new EmbedBuilder();

        builder
                .setColor(Color.GRAY)
                .setTitle("Connections Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Services:\n");
        builder.appendDescription("`vk`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`open` `<service>`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
