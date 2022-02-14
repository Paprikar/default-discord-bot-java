package dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command;

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
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class ConfigWizardCategoriesOpenCommand implements ConfigWizardCategoriesCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesOpenCommand.class);

    private static final String NAME = "open";

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoriesOpenCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        List<DiscordCategory> categories = categoryService.findAllByGuildId(session.getEntityId());
        DiscordCategory targetCategory = categories.stream()
                .filter(category -> Objects.equals(category.getName(), argsString))
                .findFirst()
                .orElse(null);
        if (targetCategory == null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The category with the name `" + argsString + "` does not exist")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        session.setEntityId(targetCategory.getId());

        logger.debug("Open at CATEGORIES: privateSession={}, target='{}'", session, argsString);

        return ConfigWizardState.CATEGORY;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
