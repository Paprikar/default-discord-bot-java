package dev.paprikar.defaultdiscordbot.core.session.config.validation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Component
public class ConfigWizardDiscordTextChannelIdValidator {

    public ConfigWizardValidatorProcessingResponse<Long> process(@Nonnull String value) {
        long id;

        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException e) {
            MessageEmbed error = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build();
            return new ConfigWizardValidatorProcessingResponse<>(null, error);
        }

        return new ConfigWizardValidatorProcessingResponse<>(id, null);
    }

    public MessageEmbed test(@Nonnull Long channelId, @Nonnull Long guildId, @Nonnull JDA jda) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null || channel.getGuild().getIdLong() != guildId) {
            return new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The text channel with the specified id `" + channelId + "` does not exist")
                    .build();
        }

        return null;
    }
}
