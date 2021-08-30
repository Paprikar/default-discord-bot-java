package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardCategoryApprovalChannelIdSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryApprovalChannelIdSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordCategory category,
                                          @Nonnull DiscordCategoryService categoryService) {
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Value has an invalid format")
                    .build()
            );
        }
        category.setApprovalChannelId(id);
        categoryService.saveCategory(category);
        logger.debug("The category={id={}} approvalChannelId is set to '{}'", category.getId(), value);
        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("ApprovalChannelId value has been set to `" + value + "`")
                .build()
        );
    }
}
