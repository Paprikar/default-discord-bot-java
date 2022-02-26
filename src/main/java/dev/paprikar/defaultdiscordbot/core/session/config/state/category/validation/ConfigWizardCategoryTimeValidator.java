package dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation;

import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * The category time validator in a configuration session.
 */
@Component
public class ConfigWizardCategoryTimeValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     *
     * @return the validator processing response
     */
    public ConfigWizardValidatorProcessingResponse<Time> process(@Nonnull String value) {
        Time time;
        try {
            time = Time.valueOf(LocalTime.parse(value));
        } catch (DateTimeParseException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        return new ConfigWizardValidatorProcessingResponse<>(time, null);
    }
}
