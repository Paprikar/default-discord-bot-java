package dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command;

import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ConfigWizardCategoriesOpenCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesOpenCommand.class);

    private final DiscordCategoryService categoryService;

    public ConfigWizardCategoriesOpenCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        List<DiscordCategory> categories = categoryService.findCategoriesByGuildId(session.getEntityId());
        DiscordCategory targetCategory = null;
        for (DiscordCategory c : categories) {
            if (c.getName().equals(argsString)) {
                targetCategory = c;
                break;
            }
        }
        if (targetCategory == null) {
            // todo illegal command response
            return null;
        }
        session.setEntityId(targetCategory.getId());

        logger.debug("Open at CATEGORIES: target='{}', session={}", argsString, session);
        return ConfigWizardState.CATEGORY;
    }
}
