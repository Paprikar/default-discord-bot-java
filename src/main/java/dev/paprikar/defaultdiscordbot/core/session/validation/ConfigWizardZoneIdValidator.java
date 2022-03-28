package dev.paprikar.defaultdiscordbot.core.session.validation;

import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;

/**
 * The zone id validator in a configuration session.
 */
@Component
public class ConfigWizardZoneIdValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<ZoneId> process(@Nonnull String value) {
        ZoneId zoneId;

        try {
            zoneId = ZoneId.of(value);
        } catch (DateTimeException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(zoneId, null);
    }
}
