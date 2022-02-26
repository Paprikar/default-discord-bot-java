package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * The vk provider name validator in a configuration session.
 */
@Component
public class ConfigWizardVkProviderNameValidator {

    private final DiscordProviderFromVkService vkProviderService;

    /**
     * Constructs a validator.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     */
    @Autowired
    public ConfigWizardVkProviderNameValidator(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     * @param provider
     *         the vk provider for processing
     *
     * @return the validator processing response
     */
    public ConfigWizardValidatorProcessingResponse<String> process(@Nonnull String value,
                                                                   @Nonnull DiscordProviderFromVk provider) {
        if (value.isEmpty()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name cannot be empty")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        if (value.length() > 32) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        if (Objects.equals(provider.getName(), value)) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Changing the name to the same one does not make sense")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        List<DiscordProviderFromVk> providers = vkProviderService.findAllByCategoryId(provider.getCategory().getId());
        for (DiscordProviderFromVk p : providers) {
            if (p.getName().equals(value)) {
                MessageEmbed error = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("The name must be unique")
                        .build();
                return new ConfigWizardValidatorProcessingResponse<>(null, error);
            }
        }

        return new ConfigWizardValidatorProcessingResponse<>(value, null);
    }
}
