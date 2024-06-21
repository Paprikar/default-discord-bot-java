package dev.paprikar.defaultdiscordbot.core.session.connections.state.vk.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnectionService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;

/**
 * The vk user id validator in a connections session.
 */
@Component
public class ConnectionsWizardVkUserIdValidator {

    private final DiscordUserVkConnectionService vkConnectionService;

    /**
     * Constructs a validator.
     *
     * @param vkConnectionService an instance of {@link DiscordUserVkConnectionService}
     */
    @Autowired
    public ConnectionsWizardVkUserIdValidator(DiscordUserVkConnectionService vkConnectionService) {
        this.vkConnectionService = vkConnectionService;
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Integer> process(@Nonnull String value) {
        int id;

        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Connections Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(id, null);
    }

    /**
     * Performs testing of the vk connection for uniqueness.
     *
     * @param userId the discord user id
     *
     * @return the validator testing response
     */
    @Nullable
    public MessageEmbed test(@Nonnull Long userId) {
        if (vkConnectionService.existsById(userId)) {
            return new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Connections Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The connection is already exists")
                    .build();
        }

        return null;
    }
}
