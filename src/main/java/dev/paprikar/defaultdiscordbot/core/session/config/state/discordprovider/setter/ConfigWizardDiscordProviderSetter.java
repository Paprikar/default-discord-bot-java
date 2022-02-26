package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The interface for configuration value setters of the discord provider state.
 */
public interface ConfigWizardDiscordProviderSetter extends ConfigWizardSetter {

    /**
     * Sets the value of the vk provider.
     *
     * @param value
     *         the value to set
     * @param provider
     *         the discord provider for change
     *
     * @return the {@link List} of setting responses
     */
    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromDiscord provider);
}
