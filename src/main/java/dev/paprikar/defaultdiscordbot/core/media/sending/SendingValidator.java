package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.validation.ConfigWizardDiscordTextChannelIdValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The component for validating sending module variables.
 */
@Component
public class SendingValidator {

    private final ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator;

    /**
     * Constructs a sending validator.
     *
     * @param textChannelIdValidator
     *         an instance of {@link ConfigWizardDiscordTextChannelIdValidator}
     */
    @Autowired
    public SendingValidator(ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator) {
        this.textChannelIdValidator = textChannelIdValidator;
    }

    /**
     * Performs a preliminary validation of module variables. In most cases a nullability check is performed.
     *
     * @param category
     *         the category to validate
     *
     * @return the {@link List} of detected validation errors
     */
    public List<MessageEmbed> validateInitially(@Nonnull DiscordCategory category) {
        List<MessageEmbed> errors = new ArrayList<>();

        if (category.getSendingChannelId() == null) {
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of `sendingChannelId` must be set")
                    .build());
        }

        if (category.getStartTime() == null) {
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of `startTime` must be set")
                    .build());
        }

        if (category.getEndTime() == null) {
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of `endTime` must be set")
                    .build());
        }

        if (category.getReserveDays() == null) {
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value of `reserveDays` must be set")
                    .build());
        }

        if (!errors.isEmpty()) {
            addFailureEmbed(errors);
        }

        return errors;
    }

    /**
     * Performs the final validation of the module variables.
     *
     * @param category
     *         the category to validate
     * @param jda
     *         an instance of {@link JDA}
     *
     * @return the {@link List} of detected validation errors
     */
    public List<MessageEmbed> validateFinally(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        MessageEmbed error = textChannelIdValidator
                .test(category.getSendingChannelId(), category.getGuild().getDiscordId(), jda);
        if (error != null) {
            errors.add(error);
            addFailureEmbed(errors);
        }

        return errors;
    }

    private void addFailureEmbed(List<MessageEmbed> errors) {
        errors.add(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Configuration Wizard Error")
                .setTimestamp(Instant.now())
                .appendDescription("Sending module was not enabled")
                .build());
    }
}
