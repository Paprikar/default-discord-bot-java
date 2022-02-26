package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation;

import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

/**
 * The vk provider group id validator in a configuration session.
 */
@Component
public class ConfigWizardVkProviderGroupIdValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     *
     * @return the validator processing response
     */
    public ConfigWizardValidatorProcessingResponse<Integer> process(@Nonnull String value) {
        int id;

        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        return new ConfigWizardValidatorProcessingResponse<>(id, null);
    }
}
