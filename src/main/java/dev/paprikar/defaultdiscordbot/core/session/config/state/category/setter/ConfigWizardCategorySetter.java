package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;

import javax.annotation.Nonnull;

public interface ConfigWizardCategorySetter {

    @Nonnull
    ConfigWizardSetterResponse set(@Nonnull String value,
                                   @Nonnull DiscordCategory category,
                                   @Nonnull DiscordCategoryService categoryService);
}
