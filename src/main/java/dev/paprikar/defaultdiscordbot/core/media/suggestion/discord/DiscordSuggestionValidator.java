package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DiscordSuggestionValidator {

    @Nonnull
    public static List<MessageEmbed> validateInitially(@Nonnull DiscordProviderFromDiscord provider) {
        List<MessageEmbed> errors = new ArrayList<>();

        Long suggestionChannelId = provider.getSuggestionChannelId();
        if (suggestionChannelId == null) {
            // todo invalid param response
        }

        return errors;
    }

    @Nonnull
    public static List<MessageEmbed> validateFinally(@Nonnull DiscordProviderFromDiscord provider, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        if (jda.getTextChannelById(provider.getSuggestionChannelId()) == null) {
            // todo invalid param response
        }

        return errors;
    }
}
