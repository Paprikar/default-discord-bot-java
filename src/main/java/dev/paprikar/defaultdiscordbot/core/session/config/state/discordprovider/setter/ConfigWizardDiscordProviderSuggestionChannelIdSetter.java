package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Component
public class ConfigWizardDiscordProviderSuggestionChannelIdSetter implements ConfigWizardDiscordProviderSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderSuggestionChannelIdSetter.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    private final DiscordSuggestionService discordSuggestionService;

    private final ReadWriteLockService readWriteLockService;

    @Autowired
    public ConfigWizardDiscordProviderSuggestionChannelIdSetter(
            DiscordProviderFromDiscordService discordProviderService,
            DiscordSuggestionService discordSuggestionService,
            ReadWriteLockService readWriteLockService) {
        this.discordProviderService = discordProviderService;
        this.discordSuggestionService = discordSuggestionService;
        this.readWriteLockService = readWriteLockService;
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
                    .appendDescription("Value has an invalid format")
                    .build()
            );
        }

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, provider.getCategory().getGuild().getId());
        if (lock == null) {
            // todo error response
            return null;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        long oldChannelId = provider.getSuggestionChannelId();
        provider.setSuggestionChannelId(newChannelId);
        discordProviderService.save(provider);

        discordSuggestionService.updateSuggestionChannel(oldChannelId, newChannelId);

        writeLock.unlock();

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
