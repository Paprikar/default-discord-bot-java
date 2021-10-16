package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Component
public class ConfigWizardCategoryEnableCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryEnableCommand.class);

    private final DiscordCategoryService categoryService;

    private final MediaActionService mediaActionService;

    private final ReadWriteLockService readWriteLockService;

    @Autowired
    public ConfigWizardCategoryEnableCommand(DiscordCategoryService categoryService,
                                             MediaActionService mediaActionService,
                                             ReadWriteLockService readWriteLockService) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
        this.readWriteLockService = readWriteLockService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Optional<DiscordCategory> categoryOptional = categoryService.findById(session.getEntityId());
        if (!categoryOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordCategory category = categoryOptional.get();

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

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, category.getGuild().getId());
        if (lock == null) {
            return null;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        category.setEnabled(true);
        categoryService.save(category);

        mediaActionService.enableCategory(category, jda);

        writeLock.unlock();

        // todo enabled response
        return null;
    }
}
