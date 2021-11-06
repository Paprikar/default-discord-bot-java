package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;

import javax.annotation.Nonnull;

public interface ConfigWizardDiscordProviderSetter extends ConfigWizardSetter {

    @Nonnull
    ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordProviderFromDiscord provider);
}
