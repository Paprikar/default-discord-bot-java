package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The command for switching from category directory to categories directory.
 */
@Component
public class ConfigWizardCategoryBackCommand implements ConfigWizardCategoryCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryBackCommand.class);

    private static final String NAME = "back";

    private final DiscordCategoryService categoryService;

    /**
     * Constructs the command.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     */
    @Autowired
    public ConfigWizardCategoryBackCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long categoryId = session.getEntityId();

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            logger.warn("execute(): Unable to get category={id={}} for privateSession={}", categoryId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordCategory category = categoryOptional.get();

        session.setEntityId(category.getGuild().getId());
        return ConfigWizardState.CATEGORIES;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
