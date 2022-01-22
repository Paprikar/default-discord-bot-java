package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
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
public class DiscordSuggestionValidator {

    private final ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator;

    @Autowired
    public DiscordSuggestionValidator(ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator) {
        this.textChannelIdValidator = textChannelIdValidator;
    }

    public List<MessageEmbed> validateInitially(@Nonnull DiscordProviderFromDiscord provider) {
        List<MessageEmbed> errors = new ArrayList<>();

        Long suggestionChannelId = provider.getSuggestionChannelId();
        if (suggestionChannelId == null) {
            // todo invalid param response
            addFailureEmbed(errors, provider);
        }

        return errors;
    }

    public List<MessageEmbed> validateFinally(@Nonnull DiscordProviderFromDiscord provider, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        MessageEmbed error = textChannelIdValidator
                .test(provider.getSuggestionChannelId(), provider.getCategory().getGuild().getDiscordId(), jda);
        if (error != null) {
            errors.add(error);
            addFailureEmbed(errors, provider);
        }

        return errors;
    }

    private void addFailureEmbed(List<MessageEmbed> errors, DiscordProviderFromDiscord provider) {
        errors.add(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Configuration Wizard Error")
                .setTimestamp(Instant.now())
                .appendDescription("Discord provider `" + provider.getName() + "` was not enabled")
                .build());
    }
}
