package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggester;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The discord user mention validator in a configuration session.
 */
@Component
public class ConfigWizardTrustedSuggestersMentionValidator {

    private final DiscordTrustedSuggesterService trustedSuggesterService;

    private final Pattern userPattern = Pattern.compile("^" + Message.MentionType.USER.getPattern().pattern() + "$");

    /**
     * Constructs a validator.
     *
     * @param trustedSuggesterService
     *         an instance of {@link DiscordTrustedSuggesterService}
     */
    @Autowired
    public ConfigWizardTrustedSuggestersMentionValidator(DiscordTrustedSuggesterService trustedSuggesterService) {
        this.trustedSuggesterService = trustedSuggesterService;
    }

    /**
     * Performs initial processing of the value.
     *
     * @param value
     *         the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Long> process(@Nonnull String value) {
        if (value.isEmpty()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The user mention cannot be empty")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        Matcher userMatcher = userPattern.matcher(value);
        if (!userMatcher.find()) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value is not an user mention")
                    .build();
            return new DiscordValidatorProcessingResponse<>(null, error);
        }
        long userId = Long.parseLong(userMatcher.group(1));

        return new DiscordValidatorProcessingResponse<>(userId, null);
    }

    /**
     * Performs testing of the user for uniqueness.
     *
     * @param userId
     *         the user id
     * @param categoryId
     *         the category id
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
