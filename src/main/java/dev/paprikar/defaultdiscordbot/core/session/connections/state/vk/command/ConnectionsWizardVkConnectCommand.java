package dev.paprikar.defaultdiscordbot.core.session.connections.state.vk.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnection;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnectionService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardState;
import dev.paprikar.defaultdiscordbot.core.session.connections.state.vk.validation.ConnectionsWizardVkUserIdValidator;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;

/**
 * The command for connecting a vk account.
 */
@Component
public class ConnectionsWizardVkConnectCommand implements ConnectionsWizardVkCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardVkConnectCommand.class);

    private static final String NAME = "connect";

    private final DiscordUserVkConnectionService vkConnectionService;
    private final ConnectionsWizardVkUserIdValidator validator;

    /**
     * Constructs the command.
     *
     * @param vkConnectionService an instance of {@link DiscordUserVkConnectionService}
     * @param validator an instance of {@link ConnectionsWizardVkUserIdValidator}
     */
    @Autowired
    public ConnectionsWizardVkConnectCommand(DiscordUserVkConnectionService vkConnectionService,
                                             ConnectionsWizardVkUserIdValidator validator) {
        this.vkConnectionService = vkConnectionService;
        this.validator = validator;
    }

    @Override
    public ConnectionsWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                          @Nonnull ConnectionsWizardSession session,
                                          String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long discordUserId = session.getUserId();
        java.util.List<MessageEmbed> responses = session.getResponses();

        DiscordValidatorProcessingResponse<Integer> response = validator.process(argsString);
        Integer vkUserId = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return ConnectionsWizardState.KEEP;
        }

        error = validator.test(discordUserId);
        if (error != null) {
            responses.add(error);
            return ConnectionsWizardState.KEEP;
        }

        DiscordUserVkConnection connection = new DiscordUserVkConnection();

        connection.setDiscordUserId(discordUserId);
        connection.setVkUserId(vkUserId);

        connection = vkConnectionService.save(connection);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The connection has been added")
                .build()
        );

        logger.debug("Add at VK: privateSession={}, connection={}", session, connection);
        return ConnectionsWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
