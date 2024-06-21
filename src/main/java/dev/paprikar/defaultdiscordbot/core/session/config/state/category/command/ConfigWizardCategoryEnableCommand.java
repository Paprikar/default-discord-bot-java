package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The command for enabling a category.
 */
@Component
public class ConfigWizardCategoryEnableCommand implements ConfigWizardCategoryCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryEnableCommand.class);

    private static final String NAME = "enable";

    private final DiscordCategoryService categoryService;
    private final MediaActionService mediaActionService;

    /**
     * Constructs the command.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param mediaActionService an instance of {@link MediaActionService}
     */
    @Autowired
    public ConfigWizardCategoryEnableCommand(DiscordCategoryService categoryService,
                                             MediaActionService mediaActionService) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long categoryId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            logger.warn("execute(): Unable to get category={id={}} for privateSession={}", categoryId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordCategory category = categoryOptional.get();

        if (category.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The category is already enabled")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        category.setEnabled(true);
        category = categoryService.save(category);

        List<MessageEmbed> errors = mediaActionService.enableCategory(category);
        responses.addAll(errors);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The category has been enabled")
                .build());

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
