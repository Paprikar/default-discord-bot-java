package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Component
public class ConfigWizardDiscordProviderSuggestionChannelIdSetter implements ConfigWizardDiscordProviderSetter {

    private static final String VARIABLE_NAME = "suggestionChannelId";

    private static final Logger logger = LoggerFactory.getLogger(
            ConfigWizardDiscordProviderSuggestionChannelIdSetter.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    private final DiscordSuggestionService discordSuggestionService;

    @Autowired
    public ConfigWizardDiscordProviderSuggestionChannelIdSetter(
            DiscordProviderFromDiscordService discordProviderService,
            DiscordSuggestionService discordSuggestionService) {
        this.discordProviderService = discordProviderService;
        this.discordSuggestionService = discordSuggestionService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordProviderFromDiscord provider) {
        long newChannelId;
        try {
            newChannelId = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build()
            );
        }

        long oldChannelId = provider.getSuggestionChannelId();
        provider.setSuggestionChannelId(newChannelId);
        provider = discordProviderService.save(provider);

        discordSuggestionService.update(oldChannelId, newChannelId);

        logger.debug("The discordProvider={id={}} value 'suggestionChannelId' is set to '{}'", provider.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `suggestionChannelId` has been set to `" + value + "`")
                .build()
        );
    }

    @Nonnull
    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
