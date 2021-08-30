package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;

import javax.annotation.Nonnull;

public interface ConfigWizardVkProviderSetter {

    @Nonnull
    ConfigWizardSetterResponse set(@Nonnull String value,
                                   @Nonnull DiscordProviderFromVk provider,
                                   @Nonnull DiscordProviderFromVkService vkProviderService);
}
