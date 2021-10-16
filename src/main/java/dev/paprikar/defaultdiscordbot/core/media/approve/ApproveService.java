package dev.paprikar.defaultdiscordbot.core.media.approve;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.*;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Service
public class ApproveService {

    private final Logger logger = LoggerFactory.getLogger(ApproveService.class);

    private final DiscordGuildService guildService;

    private final DiscordCategoryService categoryService;

    private final DiscordMediaRequestService mediaRequestService;

    private final SendingService sendingService;

    private final LockService lockService;

    private final ReadWriteLockService readWriteLockService;

    // Map<ApprovalChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    public ApproveService(DiscordGuildService guildService,
                          DiscordCategoryService categoryService,
                          DiscordMediaRequestService mediaRequestService,
                          SendingService sendingService,
                          LockService lockService,
                          ReadWriteLockService readWriteLockService) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.sendingService = sendingService;
        this.lockService = lockService;
        this.readWriteLockService = readWriteLockService;
    }

    public void handle(@Nonnull TextChannelDeleteEvent event) {
        Optional<DiscordGuild> guildOptional = guildService.findByDiscordId(event.getGuild().getIdLong());
        if (!guildOptional.isPresent()) {
            return;
        }

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, guildOptional.get().getId());
        if (lock == null) {
            return;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        categories.remove(event.getChannel().getIdLong());

        writeLock.unlock();
    }

    public void handle(@Nonnull GuildMessageReactionAddEvent event) {
        MessageReaction.ReactionEmote emote = event.getReactionEmote();
        if (emote.isEmote()) {
            return;
        }
        String emoji = emote.getEmoji();

        Long categoryId = categories.get(event.getChannel().getIdLong());
        if (categoryId == null) {
            return;
        }

        Lock lock = lockService.get(LockKey.from(LockScope.CATEGORY_APPROVE, categoryId));
        if (lock == null) {
            return;
        }

        lock.lock();

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            lock.unlock();
            return;
        }
        DiscordCategory category = categoryOptional.get();

        lock.lock();
        event.retrieveMessage().queue(message -> onMessageRetrievalSuccess(message, category, emoji, lock),
                throwable -> onReactionProcessingFailure(throwable, lock));
    }

    public void approve(DiscordCategory category, String content) {
        mediaRequestService.save(new DiscordMediaRequest(category, content));
        sendingService.updateSender(category);
    }

    public void addCategory(@Nonnull DiscordCategory category) {
        lockService.add(LockScope.CATEGORY_APPROVE, category.getId());
        categories.put(category.getApprovalChannelId(), category.getId());
    }

    public void removeCategory(@Nonnull DiscordCategory category) {
        lockService.remove(LockScope.CATEGORY_APPROVE, category.getId());
        categories.remove(category.getApprovalChannelId());
    }

    public void updateCategory(@Nonnull DiscordCategory category) {
        Long channelId = category.getApprovalChannelId();
        categories.put(channelId, category.getId());
        categories.remove(channelId);
    }

    // can usually occur when the connection is lost or the channel is unavailable
    private void onReactionProcessingFailure(Throwable throwable, Lock lock) {
        String message = "Failed to process approval request";
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException ere = (ErrorResponseException) throwable;
            if (ere.isServerError()
                    || ere.getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL
                    || ere.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                logger.warn(message, throwable);
            } else {
                logger.error(message, throwable);
            }
        } else {
            logger.error(message, throwable);
        }
        lock.unlock();
    }

    private void onMessageRetrievalSuccess(Message message, DiscordCategory category, String emoji, Lock lock) {
        String positiveEmoji = category.getPositiveApprovalEmoji().toString();
        String negativeEmoji = category.getNegativeApprovalEmoji().toString();

        if (emoji.equals(positiveEmoji)) {
            message.delete().queue(unused -> onMessageDeletionSuccess(message, category, lock),
                    throwable -> onReactionProcessingFailure(throwable, lock));
        } else if (emoji.equals(negativeEmoji)) {
            message.delete().queue(unused -> onMessageDeletionFailure(lock),
                    throwable -> onReactionProcessingFailure(throwable, lock));
        }
    }

    private void onMessageDeletionSuccess(Message message, DiscordCategory category, Lock lock) {
        // todo suggester info
        approve(category, message.getContentRaw());
        lock.unlock();
    }

    private void onMessageDeletionFailure(Lock lock) {
        lock.unlock();
    }
}
