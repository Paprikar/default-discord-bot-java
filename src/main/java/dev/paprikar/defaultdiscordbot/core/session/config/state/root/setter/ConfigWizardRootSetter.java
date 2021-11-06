package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;

import javax.annotation.Nonnull;

public interface ConfigWizardRootSetter extends ConfigWizardSetter {

    @Nonnull
    ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordGuild guild);
}
