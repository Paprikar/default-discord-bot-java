package dev.paprikar.defaultdiscordbot.core.session.validation;

import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;

/**
 * The discord text channel id validator in a configuration session.
 */
@Component
public class ConfigWizardDiscordTextChannelIdValidator {

    /**
     * Performs initial processing of the value.
     *
     * @param value the value to be processed
     *
     * @return the validator processing response
     */
    public DiscordValidatorProcessingResponse<Long> process(@Nonnull String value) {
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
            return new DiscordValidatorProcessingResponse<>(null, error);
        }

        return new DiscordValidatorProcessingResponse<>(id, null);
    }

    /**
     * Performs testing of channel id within the guild.
     *
     * @param channelId the channel id to be tested
     * @param guildId the guild id for testing
     * @param jda an instance of {@link JDA} for testing
     *
     * @return the validator testing response
     */
    @Nullable
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
