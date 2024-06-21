package dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command;

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
import java.util.Objects;

/**
 * The command to remove categories.
 */
@Component
public class ConfigWizardCategoriesRemoveCommand implements ConfigWizardCategoriesCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesRemoveCommand.class);

    private static final String NAME = "remove";

    private final DiscordCategoryService categoryService;

    /**
     * Constructs the command.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     */
    @Autowired
    public ConfigWizardCategoriesRemoveCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long guildId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        List<DiscordCategory> categories = categoryService.findAllByGuildId(guildId);
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

        if (targetCategory.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The category that is enabled cannot be deleted")
                    .build()
            );
            return ConfigWizardState.KEEP;
        }

        categoryService.delete(targetCategory);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The category `" + targetCategory.getName() + "` has been removed")
                .build()
        );

        logger.debug("The category={id={}} was deleted", targetCategory.getId());
        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
