package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation.ConfigWizardVkProviderCredsValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The component for validating vk suggestion module variables.
 */
@Component
public class VkSuggestionValidator {

    private final ConfigWizardVkProviderCredsValidator vkProviderCredsValidator;

    /**
     * Constructs a vk suggestion validator.
     *
     * @param vkProviderCredsValidator
     *         an instance of {@link ConfigWizardVkProviderCredsValidator}
     */
    @Autowired
    public VkSuggestionValidator(ConfigWizardVkProviderCredsValidator vkProviderCredsValidator) {
        this.vkProviderCredsValidator = vkProviderCredsValidator;
    }

    /**
     * Performs a preliminary validation of module variables. In most cases a nullability check is performed.
     *
     * @param provider
     *         the provider to validate
     *
     * @return the {@link List} of detected validation errors
     */
    public List<MessageEmbed> validateInitially(@Nonnull DiscordProviderFromVk provider) {
        List<MessageEmbed> errors = new ArrayList<>();

        Integer groupId = provider.getGroupId();
        if (groupId == null) {
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of `groupId` must be set")
                    .build());
        }

        String token = provider.getToken();
        if (token == null) {
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of `token` must be set")
                    .build());
        }

        if (!errors.isEmpty()) {
            addFailureEmbed(errors, provider);
        }

        return errors;
    }

    /**
     * Performs the final validation of the module variables.
     *
     * @param provider
     *         the provider to validate
     *
     * @return the {@link List} of detected validation errors
     */
    public List<MessageEmbed> validateFinally(@Nonnull DiscordProviderFromVk provider) {
        List<MessageEmbed> errors = new ArrayList<>();


        MessageEmbed error = vkProviderCredsValidator.test(provider.getGroupId(), provider.getToken());
        if (error != null) {
            errors.add(error);
            addFailureEmbed(errors, provider);
        }

        return errors;
    }

    private void addFailureEmbed(List<MessageEmbed> errors, DiscordProviderFromVk provider) {
        errors.add(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Configuration Wizard Error")
                .setTimestamp(Instant.now())
                .appendDescription("Vk provider `" + provider.getName() + "` was not enabled")
                .build());
    }
}
