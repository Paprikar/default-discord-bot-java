package dev.paprikar.defaultdiscordbot.core.session.config.state.categories;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command.ConfigWizardCategoriesCommand;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for the configuration state of categories directory.
 */
@Service
public class ConfigWizardCategoriesService extends ConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesService.class);

    private final DiscordCategoryService categoryService;

    /**
     * Constructs a configuration state service.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param commands a {@link List} of instances of {@link ConfigWizardCategoriesCommand}
     */
    @Autowired
    public ConfigWizardCategoriesService(DiscordCategoryService categoryService,
                                         List<ConfigWizardCategoriesCommand> commands) {
        super();

        this.categoryService = categoryService;

        commands.forEach(command -> this.commands.put(command.getName(), command));
    }

    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull ConfigWizardSession session) {
        logger.trace("handle(): privateSession={}", session);

        return super.handle(event, session);
    }

    @Transactional
    @Override
    public void print(@Nonnull ConfigWizardSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Long guildId = session.getEntityId();
            MessageEmbed embed = getDescription(categoryService.findAllByGuildId(guildId));
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel()
                    .sendMessageEmbeds(responses)
                    .queue(null, printingErrorHandler);
            session.setResponses(new ArrayList<>());
        }
    }

    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.CATEGORIES;
    }

    private MessageEmbed getDescription(List<DiscordCategory> categories) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories`\n\n");

        if (!categories.isEmpty()) {
            builder.appendDescription("Categories:\n");
            categories.stream()
                    .map(category -> "`" + category.getName() + "`\n")
                    .forEach(builder::appendDescription);
            builder.appendDescription("\n");
        }

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`open` `<name>`\n");
        builder.appendDescription("`add` `<name>`\n");
        builder.appendDescription("`remove` `<name>`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
