package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The interface for configuration value setters of the root state.
 */
public interface ConfigWizardRootSetter extends ConfigWizardSetter {


    /**
     * Sets the value of the guild.
     *
     * @param value
     *         the value to set
     * @param guild
     *         the guild for change
     *
     * @return the {@link List} of setting responses
     */
    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordGuild guild);
}
