package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Component
public class ConfigWizardCategoryDisableCommand implements ConfigWizardCategoryCommand {

    private static final String NAME = "disable";

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryDisableCommand.class);

    private final DiscordCategoryService categoryService;

    private final MediaActionService mediaActionService;

    @Autowired
    public ConfigWizardCategoryDisableCommand(DiscordCategoryService categoryService,
                                              MediaActionService mediaActionService) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Optional<DiscordCategory> categoryOptional = categoryService.findById(session.getEntityId());
        if (!categoryOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordCategory category = categoryOptional.get();

        if (!category.isEnabled()) {
            // todo already disabled response
            return null;
        }

        category.setEnabled(false);
        category = categoryService.save(category);

        mediaActionService.disableCategory(category);

        // todo disabled response

        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
