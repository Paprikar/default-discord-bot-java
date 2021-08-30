package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

public class ConfigWizardCategoryNameSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryNameSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordCategory category,
                                          @Nonnull DiscordCategoryService categoryService) {
        if (value.length() > 32) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build()
            );
        }
        if (category.getName().equals(value)) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Changing category name to the same one does not make sense")
                    .build()
            );
        }
        List<DiscordCategory> categories = categoryService.findCategoriesByGuildId(category.getGuild().getId());
        for (DiscordCategory c : categories) {
            if (c.getName().equals(value)) {
                return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("Category name must be unique")
                        .build()
                );
            }
        }
        category.setName(value);
        categoryService.saveCategory(category);
        logger.debug("The category={id={}} name is set to '{}'", category.getId(), value);
        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Name value has been set to `" + value +"`")
                .build()
        );
    }
}
