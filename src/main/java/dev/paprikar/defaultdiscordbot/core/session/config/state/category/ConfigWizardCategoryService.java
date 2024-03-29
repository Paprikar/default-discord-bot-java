package dev.paprikar.defaultdiscordbot.core.session.config.state.category;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.command.ConfigWizardCategoryCommand;
import dev.paprikar.defaultdiscordbot.utils.DateTimeConversions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import java.awt.*;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for the configuration state of category directory.
 */
@Service
public class ConfigWizardCategoryService extends ConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryService.class);

    private final DiscordCategoryService categoryService;

    /**
     * Constructs a configuration state service.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param commands
     *         a {@link List} of instances of {@link ConfigWizardCategoryCommand}
     */
    @Autowired
    public ConfigWizardCategoryService(DiscordCategoryService categoryService,
                                       List<ConfigWizardCategoryCommand> commands) {
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
            Long categoryId = session.getEntityId();
            Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
            if (categoryOptional.isEmpty()) {
                logger.warn("print(): Unable to get category={id={}} for privateSession={}", categoryId, session);
                return;
            }
            DiscordCategory category = categoryOptional.get();

            MessageEmbed embed = getDescription(category);
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
        return ConfigWizardState.CATEGORY;
    }

    private MessageEmbed getDescription(DiscordCategory category) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "`\n\n");

        String state = category.isEnabled() ? "enabled" : "disabled";
        builder.appendDescription("Current state: `" + state + "`\n\n");

        ZoneId zoneId = category.getGuild().getZoneId();

        Time startTime = category.getStartTime();
        LocalTime startTimeLocal = startTime == null ? null : DateTimeConversions
                .convertLocalTimeForZones(startTime.toLocalTime(), ZoneOffset.UTC, zoneId);

        Time endTime = category.getEndTime();
        LocalTime endTimeLocal = endTime == null ? null : DateTimeConversions
                .convertLocalTimeForZones(endTime.toLocalTime(), ZoneOffset.UTC, zoneId);

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + category.getName() + "`\n");
        builder.appendDescription("`sendingChannelId` = `" + category.getSendingChannelId() + "`\n");
        builder.appendDescription("`approvalChannelId` = `" + category.getApprovalChannelId() + "`\n");
        builder.appendDescription("`startTime` = `" + startTimeLocal + "`\n");
        builder.appendDescription("`endTime` = `" + endTimeLocal + "`\n");
        builder.appendDescription("`reserveDays` = `" + category.getReserveDays() + "`\n");
        builder.appendDescription("`positiveApprovalEmoji` = `" + category.getPositiveApprovalEmoji() + "`\n");
        builder.appendDescription("`negativeApprovalEmoji` = `" + category.getNegativeApprovalEmoji() + "`\n\n");

        builder.appendDescription("Directories:\n");
        builder.appendDescription("`discord providers`\n");
        builder.appendDescription("`vk providers`\n");
        builder.appendDescription("`trusted suggesters`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`open` `<directory>`\n");
        builder.appendDescription("`enable`\n");
        builder.appendDescription("`disable`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
