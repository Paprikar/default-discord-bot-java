package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ConfigWizardCategoryRemoveCommand implements ConfigWizardCategoryCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryRemoveCommand.class);

    private static final String NAME = "remove";

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoryRemoveCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordCategory> categoryOptional = categoryService.findById(entityId);
        if (categoryOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending privateSession={}", entityId, session);

            return ConfigWizardState.END;
        }
        DiscordCategory category = categoryOptional.get();

        if (category.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The category that is enabled cannot be deleted")
                    .build()
            );
            return null;
        }

        session.setEntityId(category.getGuild().getId());
        categoryService.detach(category);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The category `" + category.getName() + "` has been successfully deleted")
                .build()
        );

        logger.debug("The category={id={}} was deleted", category.getId());
        return ConfigWizardState.CATEGORIES;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
