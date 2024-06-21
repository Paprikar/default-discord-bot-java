package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

/**
 * The interface for configuration value setters of the trusted suggesters state.
 */
public interface ConfigWizardTrustedSuggestersSetter extends ConfigWizardSetter {

    /**
     * Sets the value of the trusted suggesters.
     *
     * @param value the value to set
     * @param category the category for change
     *
     * @return the {@link List} of setting responses
     */
    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordCategory category);
}
