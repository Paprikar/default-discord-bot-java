package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SendingValidator {

    @Nonnull
    public static List<MessageEmbed> validateInitially(@Nonnull DiscordCategory category) {
        List<MessageEmbed> errors = new ArrayList<>();

        if (category.getSendingChannelId() == null) {
            // todo invalid param error
        }

        // todo time checks

        if (category.getStartTime() == null) {
            // todo invalid param error
        }

        if (category.getEndTime() == null) {
            // todo invalid param error
        }

        Integer reserveDays = category.getReserveDays();
        if (reserveDays == null) {
            // todo invalid param error
        } else if (reserveDays < 1) {
            // todo invalid param error
        }

        return errors;
    }

    @Nonnull
    public static List<MessageEmbed> validateFinally(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        if (jda.getTextChannelById(category.getSendingChannelId()) == null) {
            // todo invalid param response
        }

        return errors;
    }
}
