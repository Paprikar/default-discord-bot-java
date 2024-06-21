package dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation;

import com.vdurmont.emoji.EmojiParser;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * The category approval emoji validator in a configuration session.
 */
@Component
public class ConfigWizardCategoryApprovalEmojiValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Character> process(@Nonnull String value) {
        if (value.length() > 1) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of emoji can consist of only one character")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }
        List<String> emojis = EmojiParser.extractEmojis(value);

        if (emojis.isEmpty()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value is not an emoji")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(value.charAt(0), null);
    }
}
