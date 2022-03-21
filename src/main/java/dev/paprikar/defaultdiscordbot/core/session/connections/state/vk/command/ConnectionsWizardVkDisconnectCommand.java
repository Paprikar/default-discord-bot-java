package dev.paprikar.defaultdiscordbot.core.session.connections.state.vk.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnection;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnectionService;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The command for disconnecting a vk account.
 */
@Component
public class ConnectionsWizardVkDisconnectCommand implements ConnectionsWizardVkCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardVkDisconnectCommand.class);

    private static final String NAME = "disconnect";

    private final DiscordUserVkConnectionService vkConnectionService;

    /**
     * Constructs the command.
     *
     * @param vkConnectionService
     *         an instance of {@link DiscordUserVkConnectionService}
     */
    @Autowired
    public ConnectionsWizardVkDisconnectCommand(DiscordUserVkConnectionService vkConnectionService) {
        this.vkConnectionService = vkConnectionService;
    }

    @Override
    public ConnectionsWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                          @Nonnull ConnectionsWizardSession session,
                                          String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long userId = session.getUserId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordUserVkConnection> connectionOptional = vkConnectionService.findById(userId);
        if (connectionOptional.isEmpty()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Connections Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The connection does not exist")
                    .build());

            return ConnectionsWizardState.KEEP;
        }
        DiscordUserVkConnection connection = connectionOptional.get();

        vkConnectionService.delete(connection);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The connection has been removed")
                .build()
        );

        logger.debug("The connection={id={}} was deleted", connection.getDiscordUserId());
        return ConnectionsWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
