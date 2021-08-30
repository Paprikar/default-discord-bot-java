package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;

import javax.annotation.Nonnull;

public interface ConfigWizardRootSetter {

    @Nonnull
    ConfigWizardSetterResponse set(@Nonnull String value,
                                   @Nonnull DiscordGuildService guildService,
                                   @Nonnull DiscordGuild guild);
}
