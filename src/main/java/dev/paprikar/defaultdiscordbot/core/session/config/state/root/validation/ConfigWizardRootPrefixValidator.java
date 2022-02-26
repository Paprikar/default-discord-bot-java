package dev.paprikar.defaultdiscordbot.core.session.config.state.root.validation;

import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

/**
 * The guild prefix validator in a configuration session.
 */
@Component
public class ConfigWizardRootPrefixValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     *
     * @return the validator processing response
     */
    public ConfigWizardValidatorProcessingResponse<String> process(@Nonnull String value) {
        if (value.length() > 32) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the prefix cannot be more than 32 characters")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        return new ConfigWizardValidatorProcessingResponse<>(value, null);
    }
}
