package dev.paprikar.defaultdiscordbot.core.media.approve;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ApproveValidator {

    @Nonnull
    public static List<MessageEmbed> validateInitially(@Nonnull DiscordCategory category) {
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

        return errors;
    }

    @Nonnull
    public static List<MessageEmbed> validateFinally(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        if (jda.getTextChannelById(category.getApprovalChannelId()) == null) {
            // todo invalid param response
        }

        return errors;
    }
}
