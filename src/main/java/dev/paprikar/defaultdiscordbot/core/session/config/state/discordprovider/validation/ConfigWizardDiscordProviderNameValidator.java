package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
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
 * The discord provider name validator in a configuration session.
 */
@Component
public class ConfigWizardDiscordProviderNameValidator {

    private final DiscordProviderFromDiscordService discordProviderService;

    /**
     * Constructs a validator.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     */
    @Autowired
    public ConfigWizardDiscordProviderNameValidator(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     * @param provider
     *         the discord provider for processing
     *
     * @return the validator processing response
     */
    public ConfigWizardValidatorProcessingResponse<String> process(@Nonnull String value,
                                                                   @Nonnull DiscordProviderFromDiscord provider) {
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

        Long categoryId = provider.getCategory().getId();
        List<DiscordProviderFromDiscord> providers = discordProviderService.findAllByCategoryId(categoryId);
        for (DiscordProviderFromDiscord p : providers) {
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
