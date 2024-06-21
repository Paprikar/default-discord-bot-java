package dev.paprikar.defaultdiscordbot.core.session.validation;

import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The time validator in a configuration session.
 */
@Component
public class ConfigWizardTimeValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Time> process(@Nonnull String value) {
        Time time;

        try {
            time = Time.valueOf(LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME));
        } catch (DateTimeParseException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(time, null);
    }
}
