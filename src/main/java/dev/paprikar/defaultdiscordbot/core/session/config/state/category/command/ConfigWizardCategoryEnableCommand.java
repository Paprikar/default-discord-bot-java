package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaAction;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigWizardCategoryEnableCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryEnableCommand.class);

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromDiscordService discordProviderService;

    private final DiscordSuggestionService discordSuggestionService;

    public ConfigWizardCategoryEnableCommand(DiscordCategoryService categoryService,
                                             DiscordProviderFromDiscordService discordProviderService,
                                             DiscordSuggestionService discordSuggestionService) {
        this.categoryService = categoryService;
        this.discordProviderService = discordProviderService;
        this.discordSuggestionService = discordSuggestionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        DiscordCategory category = categoryService.getById(session.getEntityId());
        if (category.isEnabled()) {
            // todo already enabled response
            return null;
        }
        JDA jda = event.getJDA();
        Long sendingChannelId = category.getSendingChannelId();
        if (sendingChannelId == null || jda.getTextChannelById(sendingChannelId) == null) {
            // todo invalid param
            return null;
        }
        Long approvalChannelId = category.getApprovalChannelId();
        if (approvalChannelId == null || jda.getTextChannelById(approvalChannelId) == null) {
            // todo invalid param
            return null;
        }
        // todo time checks
        if (category.getStartTime() == null) {
            // todo invalid param
            return null;
        }
        if (category.getEndTime() == null) {
            // todo invalid param
            return null;
        }
        Integer reserveDays = category.getReserveDays();
        if (reserveDays == null || reserveDays < 1) {
            // todo invalid param
            return null;
        }
        if (category.getPositiveApprovalEmoji() == null) {
            // todo invalid param
            return null;
        }
        if (category.getNegativeApprovalEmoji() == null) {
            // todo invalid param
            return null;
        }

        category.setEnabled(true);
        categoryService.save(category);

        MediaAction.enableCategory(category, discordProviderService, discordSuggestionService);

        // todo enabled response
        return null;
    }
}
