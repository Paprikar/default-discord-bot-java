package dev.paprikar.defaultdiscordbot.core.session.connections.state.vk;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnection;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnectionService;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizard;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import dev.paprikar.defaultdiscordbot.core.session.connections.state.vk.command.ConnectionsWizardVkCommand;
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
 * Service for the connections state of vk directory.
 */
@Service
public class ConnectionsWizardVkService extends ConnectionsWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardVkService.class);

    private final DiscordUserVkConnectionService vkConnectionService;

    /**
     * Constructs a connections state service.
     *
     * @param vkConnectionService
     *         an instance of {@link DiscordUserVkConnectionService}
     * @param commands
     *         a {@link List} of instances of {@link ConnectionsWizardVkCommand}
     */
    @Autowired
    public ConnectionsWizardVkService(DiscordUserVkConnectionService vkConnectionService,
                                      List<ConnectionsWizardVkCommand> commands) {
        super();
        this.vkConnectionService = vkConnectionService;

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
            Long userId = session.getUserId();
            DiscordUserVkConnection connection = vkConnectionService.findById(userId).orElse(null);

            MessageEmbed embed = getDescription(connection);
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
        return ConnectionsWizardState.VK;
    }

    private MessageEmbed getDescription(DiscordUserVkConnection connection) {
        EmbedBuilder builder = new EmbedBuilder();

        builder
                .setColor(Color.GRAY)
                .setTitle("Connections Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current service: `vk`\n\n");

        if (connection == null) {
            builder.appendDescription("Not connected\n\n");
        } else {
            builder.appendDescription(
                    String.format("Connected to id: [%1$s](https://vk.com/id%1$s)\n\n", connection.getVkUserId()));
        }

        builder.appendDescription("About how to check the id can be found at these links:\n");
        builder.appendDescription("https://vk.com/faq18062\n");
        builder.appendDescription("https://vk.com/linkapp\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`connect` `<id>`\n");
        builder.appendDescription("`disconnect`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
