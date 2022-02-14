package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
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

@Component
public class VkSuggestionValidator {

    private final ConfigWizardVkProviderCredsValidator vkProviderCredsValidator;

    @Autowired
    public VkSuggestionValidator(ConfigWizardVkProviderCredsValidator vkProviderCredsValidator) {
        this.vkProviderCredsValidator = vkProviderCredsValidator;
    }

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
