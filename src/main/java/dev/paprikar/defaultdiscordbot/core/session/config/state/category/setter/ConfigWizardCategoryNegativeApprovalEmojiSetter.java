package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import com.vdurmont.emoji.EmojiParser;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

public class ConfigWizardCategoryNegativeApprovalEmojiSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryNegativeApprovalEmojiSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordCategory category,
                                          @Nonnull DiscordCategoryService categoryService) {
        if (value.length() > 1) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Emoji value can consist of only one character")
                    .build()
            );
        }
        List<String> emojis = EmojiParser.extractEmojis(value);
        if (emojis.isEmpty()) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Value is not an emoji")
                    .build()
            );
        }
        category.setNegativeApprovalEmoji(value.charAt(0));
        categoryService.save(category);
        logger.debug("The category={id={}} negativeApprovalEmoji is set to '{}'", category.getId(), value);
        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("NegativeApprovalEmoji value has been set to `" + value + "`")
                .build()
        );
    }
}
