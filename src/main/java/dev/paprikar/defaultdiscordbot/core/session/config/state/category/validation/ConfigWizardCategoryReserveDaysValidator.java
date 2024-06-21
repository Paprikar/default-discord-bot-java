package dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation;

import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;

/**
 * The category reserve days validator in a configuration session.
 */
@Component
public class ConfigWizardCategoryReserveDaysValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Integer> process(@Nonnull String value) {
        int reserveDays;

        try {
            reserveDays = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        if (reserveDays < 1) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value can only be positive")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(reserveDays, null);
    }
}
