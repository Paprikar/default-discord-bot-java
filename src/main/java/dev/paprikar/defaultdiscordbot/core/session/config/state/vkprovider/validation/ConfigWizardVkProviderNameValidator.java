package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The vk provider name validator in a configuration session.
 */
@Component
public class ConfigWizardVkProviderNameValidator {

    private final DiscordProviderFromVkService vkProviderService;

    private final Pattern newLinePattern = Pattern.compile("\\n");

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
    public DiscordValidatorProcessingResponse<String> process(@Nonnull String value,
                                                              @Nonnull DiscordProviderFromVk provider) {
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
     * @param name
     *         the provider name to be tested
     * @param categoryId
     *         the category id for testing
     *
     * @return the validator testing response
     */
    @Nullable
    public MessageEmbed test(@Nonnull String name, @Nonnull Long categoryId) {
        List<DiscordProviderFromVk> providers = vkProviderService.findAllByCategoryId(categoryId);
        for (DiscordProviderFromVk provider : providers) {
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
