package dev.paprikar.defaultdiscordbot.core.session.config.state.category;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.command.ConfigWizardCategoryCommand;
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

    @Autowired
    public ConfigWizardCategoryService(DiscordCategoryService categoryService,
                                       List<ConfigWizardCategoryCommand> commands) {
        super();

        this.categoryService = categoryService;

        for (ConfigWizardCategoryCommand c : commands) {
            this.commands.put(c.getName(), c);
        }
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

        return super.handle(event, session);
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
            session.getChannel().flatMap(c -> c.sendMessageEmbeds(responses)).queue();
            session.setResponses(new ArrayList<>());
        }
    }

    @Nonnull
    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.CATEGORY;
    }
}
