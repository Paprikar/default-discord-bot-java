package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

public interface ConfigWizardDiscordProviderSetter extends ConfigWizardSetter {

    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromDiscord provider);
}
