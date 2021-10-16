package dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
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
public class ConfigWizardCategoriesAddCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesAddCommand.class);

    private final DiscordGuildService guildService;

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoriesAddCommand(DiscordGuildService guildService, DiscordCategoryService categoryService) {
        this.guildService = guildService;
        this.categoryService = categoryService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing");
            // todo internal error response
            return null;
        }

        if (argsString.isEmpty()) {
            // todo invalid input response
            return null;
        }

        if (argsString.length() > 32) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build()
            );
            return null;
        }

        long guildId = session.getEntityId();
        for (DiscordCategory c : categoryService.findAllByGuildId(guildId)) {
            if (c.getName().equals(argsString)) {
                session.getResponses().add(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("Category name must be unique")
                        .build()
                );
                return null;
            }
        }

        Optional<DiscordGuild> guildOptional = guildService.findById(guildId);
        if (!guildOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get guild={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }

        DiscordCategory category = new DiscordCategory();
        category.setName(argsString);
        category = categoryService.attach(category, guildOptional.get());

        session.setEntityId(category.getId());

        logger.debug("Add at CATEGORIES: name={}, session={}", argsString, session);

        return ConfigWizardState.CATEGORY;
    }
}
