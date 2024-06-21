package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The discord provider name validator in a configuration session.
 */
@Component
public class ConfigWizardDiscordProviderNameValidator {

    private final DiscordProviderFromDiscordService discordProviderService;

    private final Pattern newLinePattern = Pattern.compile("\\n");

    /**
     * Constructs a validator.
     *
     * @param discordProviderService an instance of {@link DiscordProviderFromDiscordService}
     */
    @Autowired
    public ConfigWizardDiscordProviderNameValidator(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     * @param provider the discord provider for processing
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<String> process(@Nonnull String value,
                                                              @Nonnull DiscordProviderFromDiscord provider) {
        if (value.isEmpty()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name cannot be empty")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        if (newLinePattern.matcher(value).find()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name cannot be multiline")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        if (value.length() > 32) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        if (value.equals(provider.getName())) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Changing the name to the same one does not make sense")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(value, null);
    }

    /**
     * Performs testing of the provider name for uniqueness.
     *
     * @param name the provider name to be tested
     * @param categoryId the category id for testing
     *
     * @return the validator testing response
     */
    @Nullable
    public MessageEmbed test(@Nonnull String name, @Nonnull Long categoryId) {
        List<DiscordProviderFromDiscord> providers = discordProviderService.findAllByCategoryId(categoryId);
        for (DiscordProviderFromDiscord provider : providers) {
            if (name.equals(provider.getName())) {
                return new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("The name must be unique")
                        .build();
            }
        }

        return null;
    }
}
