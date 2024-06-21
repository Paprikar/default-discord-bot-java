package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

/**
 * The interface for configuration value setters of the vk provider state.
 */
public interface ConfigWizardVkProviderSetter extends ConfigWizardSetter {

    /**
     * Sets the value of the vk provider.
     *
     * @param value the value to set
     * @param provider the vk provider for change
     *
     * @return the {@link List} of setting responses
     */
    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider);
}
