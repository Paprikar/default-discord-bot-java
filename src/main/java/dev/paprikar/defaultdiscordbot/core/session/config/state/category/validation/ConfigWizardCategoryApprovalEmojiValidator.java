package dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation;

import com.vdurmont.emoji.EmojiParser;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

@Component
public class ConfigWizardCategoryApprovalEmojiValidator {

    public ConfigWizardValidatorProcessingResponse<Character> process(@Nonnull String value) {
        if (value.length() > 1) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of emoji can consist of only one character")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }
        List<String> emojis = EmojiParser.extractEmojis(value);

        if (emojis.isEmpty()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value is not an emoji")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        return new ConfigWizardValidatorProcessingResponse<>(value.charAt(0), null);
    }
}
