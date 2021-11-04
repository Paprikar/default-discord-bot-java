package dev.paprikar.defaultdiscordbot.core.session.config.state.category;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
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
import java.util.Optional;

@Service
public class ConfigWizardCategoryService extends AbstractConfigWizard {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryService.class);

    private final DiscordCategoryService categoryService;

    private final ConfigWizardCategoryBackCommand backCommand;

    private final ConfigWizardCategorySetCommand setCommand;

    private final ConfigWizardCategoryOpenCommand openCommand;

    private final ConfigWizardCategoryEnableCommand enableCommand;

    private final ConfigWizardCategoryDisableCommand disableCommand;

    private final ConfigWizardCategoryRemoveCommand removeCommand;

    @Autowired
    public ConfigWizardCategoryService(DiscordCategoryService categoryService,
                                       ConfigWizardCategoryBackCommand backCommand,
                                       ConfigWizardCategorySetCommand setCommand,
                                       ConfigWizardCategoryOpenCommand openCommand,
                                       ConfigWizardCategoryEnableCommand enableCommand,
                                       ConfigWizardCategoryDisableCommand disableCommand,
                                       ConfigWizardCategoryRemoveCommand removeCommand) {
        super();

        this.categoryService = categoryService;

        this.backCommand = backCommand;
        this.setCommand = setCommand;
        this.openCommand = openCommand;
        this.enableCommand = enableCommand;
        this.disableCommand = disableCommand;
        this.removeCommand = removeCommand;

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
            Optional<DiscordCategory> categoryOptional = categoryService.findById(session.getEntityId());
            MessageEmbed embed;
            if (categoryOptional.isPresent()) {
                embed = getStateEmbed(categoryOptional.get());
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get category={id={}}", session.getEntityId());
            }
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            // todo without flatMap
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
        commands.put("back", backCommand);
        commands.put("set", setCommand);
        commands.put("open", openCommand);
        commands.put("enable", enableCommand);
        commands.put("disable", disableCommand);
        commands.put("remove", removeCommand);
    }
}
