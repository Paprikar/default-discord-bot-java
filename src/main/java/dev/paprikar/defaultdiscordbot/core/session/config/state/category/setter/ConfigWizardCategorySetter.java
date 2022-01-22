package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

public interface ConfigWizardCategorySetter extends ConfigWizardSetter {

    List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordCategory category);
}
