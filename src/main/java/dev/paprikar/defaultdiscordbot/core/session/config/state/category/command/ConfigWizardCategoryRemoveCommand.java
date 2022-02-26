package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The command for removing a category.
 */
@Component
public class ConfigWizardCategoryRemoveCommand implements ConfigWizardCategoryCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryRemoveCommand.class);

    private static final String NAME = "remove";

    private final DiscordCategoryService categoryService;

    /**
     * Constructs the command.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     */
    @Autowired
    public ConfigWizardCategoryRemoveCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordCategory> categoryOptional = categoryService.findById(entityId);
        if (categoryOptional.isEmpty()) {
            logger.warn("execute(): Unable to get category={id={}} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
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
            return ConfigWizardState.KEEP;
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
