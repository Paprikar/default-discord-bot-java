package dev.paprikar.defaultdiscordbot.core.session.config.state.category;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Service
public class ConfigWizardCategoryDescriptionService {

    public MessageEmbed getDescription(@Nonnull DiscordCategory category) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "`\n\n");

        String state = category.isEnabled() ? "enabled" : "disabled";
        builder.appendDescription("Current state: `" + state + "`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + category.getName() + "`\n");
        builder.appendDescription("`sendingChannelId` = `" + category.getSendingChannelId() + "`\n");
        builder.appendDescription("`approvalChannelId` = `" + category.getApprovalChannelId() + "`\n");
        builder.appendDescription("`startTime` = `" + category.getStartTime() + "`\n");
        builder.appendDescription("`endTime` = `" + category.getEndTime() + "`\n");
        builder.appendDescription("`reserveDays` = `" + category.getReserveDays() + "`\n");
        builder.appendDescription("`positiveApprovalEmoji` = `" + category.getPositiveApprovalEmoji() + "`\n");
        builder.appendDescription("`negativeApprovalEmoji` = `" + category.getNegativeApprovalEmoji() + "`\n\n");

        builder.appendDescription("Directories:\n");
        builder.appendDescription("`discord providers`\n");
        builder.appendDescription("`vk providers`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`open` `<directory>`\n");
        builder.appendDescription("`enable`\n");
        builder.appendDescription("`disable`\n");
        builder.appendDescription("`remove`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
