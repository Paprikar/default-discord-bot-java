package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;

import javax.annotation.Nonnull;

public interface ConfigWizardCategorySetter extends ConfigWizardSetter {

    @Nonnull
    ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category);
}
