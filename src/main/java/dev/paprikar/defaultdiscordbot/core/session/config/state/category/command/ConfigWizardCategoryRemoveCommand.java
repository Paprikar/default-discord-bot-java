package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@Component
public class ConfigWizardCategoryRemoveCommand implements ConfigWizardCategoryCommand {

    private static final String NAME = "remove";

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryRemoveCommand.class);

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoryRemoveCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
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

        if (category.isEnabled()) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Category that is enabled cannot be deleted")
                    .build()
            );
            return null;
        }

        session.setEntityId(category.getGuild().getId());
        categoryService.detach(category);

        session.getResponses().add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Category `" + category.getName() + "` has been successfully deleted")
                .build()
        );

        logger.debug("The category={id={}} was deleted", category.getId());
        return ConfigWizardState.CATEGORIES;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
