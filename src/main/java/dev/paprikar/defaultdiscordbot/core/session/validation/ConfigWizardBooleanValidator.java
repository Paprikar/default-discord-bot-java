package dev.paprikar.defaultdiscordbot.core.session.validation;

import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;

/**
 * The boolean value validator in a configuration session.
 */
@Component
public class ConfigWizardBooleanValidator {

    /**
     * Constructs a validator.
     */
    @Autowired
    public ConfigWizardBooleanValidator() {
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Boolean> process(@Nonnull String value) {
        switch (value.toLowerCase()) {
            case "true": {
                return new DiscordValidatorProcessingResponse<>(true, null);
            }
            case "false": {
                return new DiscordValidatorProcessingResponse<>(false, null);
            }
            default: {
                MessageEmbed error = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("The value has an invalid format")
                        .build();
                return new DiscordValidatorProcessingResponse<>(null, error);
            }
        }
    }
}
