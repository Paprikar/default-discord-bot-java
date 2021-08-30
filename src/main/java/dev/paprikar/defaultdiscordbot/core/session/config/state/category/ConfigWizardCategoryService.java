package dev.paprikar.defaultdiscordbot.core.session.config.state.category;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.command.*;
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
public class ConfigWizardCategoryService extends ConfigWizard {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryService.class);

    private final DiscordCategoryService categoryService;

    @Autowired
    public ConfigWizardCategoryService(DiscordGuildService guildService,
                                       DiscordCategoryService categoryService) {
        super();
        this.categoryService = categoryService;
        setupCommands();
    }

    public static MessageEmbed getStateEmbed(DiscordCategory category) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + category.getName() + "`\n");
        builder.appendDescription("`sendingChannelId` = `" + category.getSendingChannelId() + "`\n");
        builder.appendDescription("`approvalChannelId` = `" + category.getApprovalChannelId() + "`\n");
        builder.appendDescription("`startTime` = `" + category.getStartTime() + "`\n");
        builder.appendDescription("`endTime` = `" + category.getEndTime() + "`\n");
        builder.appendDescription("`reserveDays` = `" + category.getReserveDays() + "`\n");
        builder.appendDescription("`positiveApprovalEmoji` = `" + category.getPositiveApprovalEmoji() + "`\n");
        builder.appendDescription("`negativeApprovalEmoji` = `" + category.getNegativeApprovalEmoji() + "`\n\n");

        builder.appendDescription("Directories:\n");
        builder.appendDescription("`discord providers`\n");
        builder.appendDescription("`vk providers`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`open` `<directory>`\n");
        builder.appendDescription("`enable`\n");
        builder.appendDescription("`disable`\n");
        builder.appendDescription("`remove`\n");
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
            responses.add(getStateEmbed(categoryService.getCategoryById(session.getEntityId())));
        }
        if (!responses.isEmpty()) {
            session.getChannel().flatMap(c -> c.sendMessageEmbeds(responses)).queue();
            session.setResponses(new ArrayList<>());
        }
    }

    @Nonnull
    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.CATEGORY;
    }

    private void setupCommands() {
        commands.put("back", new ConfigWizardCategoryBackCommand(categoryService));
        commands.put("set", new ConfigWizardCategorySetCommand(categoryService));
        commands.put("open", new ConfigWizardCategoryOpenCommand());
        commands.put("enable", new ConfigWizardCategoryEnableCommand());
        commands.put("disable", new ConfigWizardCategoryDisableCommand());
        commands.put("remove", new ConfigWizardCategoryRemoveCommand(categoryService));
    }
}
