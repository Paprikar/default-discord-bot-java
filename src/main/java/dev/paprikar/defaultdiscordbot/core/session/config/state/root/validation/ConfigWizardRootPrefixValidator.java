package dev.paprikar.defaultdiscordbot.core.session.config.state.root.validation;

import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * The guild prefix validator in a configuration session.
 */
@Component
public class ConfigWizardRootPrefixValidator {

    private final Pattern newLinePattern = Pattern.compile("\\n");

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<String> process(@Nonnull String value) {
        if (value.length() > 32) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the prefix cannot be more than 32 characters")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        if (newLinePattern.matcher(value).find()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The prefix cannot be multiline")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(value, null);
    }
}
