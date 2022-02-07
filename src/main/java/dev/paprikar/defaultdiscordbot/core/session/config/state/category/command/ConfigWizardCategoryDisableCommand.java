package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.ConfigWizardCategoryDescriptionService;
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
public class ConfigWizardCategoryDisableCommand implements ConfigWizardCategoryCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryDisableCommand.class);

    private static final String NAME = "disable";

    private final DiscordCategoryService categoryService;
    private final MediaActionService mediaActionService;
    private final ConfigWizardCategoryDescriptionService descriptionService;

    @Autowired
    public ConfigWizardCategoryDisableCommand(DiscordCategoryService categoryService,
                                              MediaActionService mediaActionService,
                                              ConfigWizardCategoryDescriptionService descriptionService) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
        this.descriptionService = descriptionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordCategory> categoryOptional = categoryService.findById(entityId);
        if (categoryOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordCategory category = categoryOptional.get();

        if (!category.isEnabled()) {
            // todo already disabled response

            responses.add(descriptionService.getDescription(category));

            return null;
        }

        category.setEnabled(false);
        category = categoryService.save(category);

        mediaActionService.disableCategory(category);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The category has been disabled")
                .build());

        responses.add(descriptionService.getDescription(category));

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
