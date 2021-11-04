package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

@Component
public class ConfigWizardCategoryNameSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryNameSetter.class);

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoryNameSetter(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category) {
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

        List<DiscordCategory> categories = categoryService.findAllByGuildId(category.getGuild().getId());
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
        category = categoryService.save(category);

        logger.debug("The category={id={}} name is set to '{}'", category.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Name value has been set to `" + value + "`")
                .build()
        );
    }
}
