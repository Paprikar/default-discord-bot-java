package dev.paprikar.defaultdiscordbot.core.media.approve;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardDiscordTextChannelIdValidator;
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

@Component
public class ApproveValidator {

    private final ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator;

    @Autowired
    public ApproveValidator(ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator) {
        this.textChannelIdValidator = textChannelIdValidator;
    }

    public List<MessageEmbed> validateInitially(@Nonnull DiscordCategory category) {
        List<MessageEmbed> errors = new ArrayList<>();

        if (category.getApprovalChannelId() == null) {
            // todo invalid param response
        }

        if (category.getPositiveApprovalEmoji() == null) {
            // todo invalid param response
        }

        if (category.getNegativeApprovalEmoji() == null) {
            // todo invalid param response
        }

        if (!errors.isEmpty()) {
            addFailureEmbed(errors);
        }

        return errors;
    }

    public List<MessageEmbed> validateFinally(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        MessageEmbed error = textChannelIdValidator
                .test(category.getApprovalChannelId(), category.getGuild().getDiscordId(), jda);
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
                .appendDescription("Approve module was not enabled")
                .build());
    }
}
