package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;

public interface ConfigWizardRootSetter extends ConfigWizardSetter {

    MessageEmbed set(@Nonnull String value, @Nonnull DiscordGuild guild);
}
