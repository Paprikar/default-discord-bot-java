package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardDiscordProviderSuggestionChannelIdSetter implements ConfigWizardDiscordProviderSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderSuggestionChannelIdSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordProviderFromDiscord provider,
                                          @Nonnull DiscordProviderFromDiscordService discordProviderService) {
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Value has an invalid format")
                    .build()
            );
        }
        provider.setSuggestionChannelId(id);
        discordProviderService.saveProvider(provider);
        logger.debug("The discordProvider={id={}} suggestionChannelId is set to '{}'", provider.getId(), value);
        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("SuggestionChannelId value has been set to `" + value + "`")
                .build()
        );
    }
}
