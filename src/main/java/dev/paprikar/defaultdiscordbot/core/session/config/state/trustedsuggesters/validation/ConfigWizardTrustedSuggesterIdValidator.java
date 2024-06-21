package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggester;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * The trusted suggester id validator in a configuration session.
 */
@Component
public class ConfigWizardTrustedSuggesterIdValidator {

    private final DiscordTrustedSuggesterService trustedSuggesterService;

    /**
     * Constructs a validator.
     *
     * @param trustedSuggesterService an instance of {@link DiscordTrustedSuggesterService}
     */
    @Autowired
    public ConfigWizardTrustedSuggesterIdValidator(DiscordTrustedSuggesterService trustedSuggesterService) {
        this.trustedSuggesterService = trustedSuggesterService;
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Long> process(@Nonnull String value) {
        long userId;

        try {
            userId = Long.parseLong(value);
        } catch (NumberFormatException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(userId, null);
    }

    /**
     * Performs testing of the user for uniqueness.
     *
     * @param userId the user id
     * @param categoryId the category id
     *
     * @return the validator testing response
     */
    @Nullable
    public MessageEmbed test(@Nonnull Long userId, @Nonnull Long categoryId) {
        List<DiscordTrustedSuggester> suggesters = trustedSuggesterService.findAllByCategoryId(categoryId);
        for (DiscordTrustedSuggester suggester : suggesters) {
            if (userId.equals(suggester.getUserId())) {
                return new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("The suggester must be unique")
                        .build();
            }
        }

        return null;
    }
}
