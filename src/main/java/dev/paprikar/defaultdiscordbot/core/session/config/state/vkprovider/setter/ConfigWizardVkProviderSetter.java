package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

public interface ConfigWizardVkProviderSetter extends ConfigWizardSetter {

    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider);
}
