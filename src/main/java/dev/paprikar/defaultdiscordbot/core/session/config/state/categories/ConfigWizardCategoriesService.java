package dev.paprikar.defaultdiscordbot.core.session.config.state.categories;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command.ConfigWizardCategoriesAddCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command.ConfigWizardCategoriesBackCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command.ConfigWizardCategoriesOpenCommand;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigWizardCategoriesService extends AbstractConfigWizard {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesService.class);

    private final DiscordCategoryService categoryService;

    private final ConfigWizardCategoriesBackCommand backCommand;

    private final ConfigWizardCategoriesAddCommand addCommand;

    private final ConfigWizardCategoriesOpenCommand openCommand;

    @Autowired
    public ConfigWizardCategoriesService(DiscordCategoryService categoryService,
                                         ConfigWizardCategoriesBackCommand backCommand,
                                         ConfigWizardCategoriesAddCommand addCommand,
                                         ConfigWizardCategoriesOpenCommand openCommand) {
        super();

        this.categoryService = categoryService;

        this.backCommand = backCommand;
        this.addCommand = addCommand;
        this.openCommand = openCommand;

        setupCommands();
    }

    public static MessageEmbed getStateEmbed(List<DiscordCategory> categories) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories`\n\n");

        if (!categories.isEmpty()) {
            builder.appendDescription("Categories:\n");
            for (DiscordCategory c : categories) {
                builder.appendDescription(String.format("`%s`\n", c.getName()));
            }
            builder.appendDescription("\n");
        }

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`open` `<name>`\n");
        builder.appendDescription("`add` `<name>`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }

    @Nullable
    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull PrivateSession session) {
        logger.trace("handle(): event={}, sessionInfo={}", event, session);

        String message = event.getMessage().getContentRaw();
        FirstWordAndOther parts = new FirstWordAndOther(message);
        String commandName = parts.getFirstWord().toLowerCase();
        String argsString = parts.getOther();

        ConfigWizardCommand command = commands.get(commandName);
        if (command == null) {
            // todo illegal command response ?
            return null;
        }
        return command.execute(event, session, argsString);
    }

    @Transactional
    @Override
    public void print(@Nonnull PrivateSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();
        if (addStateEmbed) {
            responses.add(getStateEmbed(categoryService.findAllByGuildId(session.getEntityId())));
        }
        if (!responses.isEmpty()) {
            session.getChannel().flatMap(c -> c.sendMessageEmbeds(responses)).queue();
            session.setResponses(new ArrayList<>());
        }
    }

    @Nonnull
    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.CATEGORIES;
    }

    private void setupCommands() {
        commands.put("back", backCommand);
        commands.put("add", addCommand);
        commands.put("open", openCommand);
    }
}
