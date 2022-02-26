package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The interface for configuration value setters of the category state.
 */
public interface ConfigWizardCategorySetter extends ConfigWizardSetter {

    /**
     * Sets the value of the category.
     *
     * @param value
     *         the value to set
     * @param category
     *         the category for change
     *
     * @return the {@link List} of setting responses
     */
    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordCategory category);
}
