package dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
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


@Component
public class ConfigWizardCategoryNameValidator {

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoryNameValidator(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public ConfigWizardValidatorProcessingResponse<String> process(@Nonnull String value,
                                                                   @Nonnull DiscordCategory category) {
        if (value.isEmpty()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name can not be empty")
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

        if (Objects.equals(category.getName(), value)) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Changing the name to the same one does not make sense")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        List<DiscordCategory> categories = categoryService.findAllByGuildId(category.getGuild().getId());
        for (DiscordCategory c : categories) {
            if (c.getName().equals(value)) {
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
