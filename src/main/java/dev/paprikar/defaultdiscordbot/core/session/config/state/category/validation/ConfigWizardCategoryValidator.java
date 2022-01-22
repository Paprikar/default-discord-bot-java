package dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardDiscordTextChannelIdValidator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigWizardCategoryValidator {

    private final ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator;

    @Autowired
    public ConfigWizardCategoryValidator(ConfigWizardDiscordTextChannelIdValidator textChannelIdValidator) {
        this.textChannelIdValidator = textChannelIdValidator;
    }

    public List<MessageEmbed> test(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();
        Long guildId = category.getGuild().getDiscordId();

        errors.add(textChannelIdValidator.test(category.getSendingChannelId(), guildId, jda));
        errors.add(textChannelIdValidator.test(category.getApprovalChannelId(), guildId, jda));

        return errors;
    }
}
