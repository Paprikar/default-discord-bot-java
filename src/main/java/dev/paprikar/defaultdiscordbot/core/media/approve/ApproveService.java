package dev.paprikar.defaultdiscordbot.core.media.approve;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.concurrency.SemaphoreService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ApproveService {

    private final Logger logger = LoggerFactory.getLogger(ApproveService.class);

    private final DiscordCategoryService categoryService;

    private final DiscordMediaRequestService mediaRequestService;

    private final SendingService sendingService;

    private final LockService lockService;

    private final SemaphoreService semaphoreService;

    // Map<ApprovalChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    // Map<CategoryId, ApprovalChannelId>
    private final Map<Long, Long> approvalChannels = new ConcurrentHashMap<>();

    @Autowired
    public ApproveService(DiscordCategoryService categoryService,
                          DiscordMediaRequestService mediaRequestService,
                          SendingService sendingService,
                          LockService lockService,
                          SemaphoreService semaphoreService) {
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.sendingService = sendingService;
        this.lockService = lockService;
        this.semaphoreService = semaphoreService;
    }

    public void handle(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();
        Long categoryId = categories.get(channelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey lockKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(channelId);
        approvalChannels.remove(categoryId);

        semaphoreService.remove(ConcurrencyScope.CATEGORY_APPROVE, categoryId);

        lockService.remove(lockKey);

        lock.unlock();
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

        Semaphore semaphore = semaphoreService.get(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        if (semaphore == null) {
            return;
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            logger.error("The semaphore release was interrupted", e);
            return;
        }

        event.retrieveMessage().queue(message -> onMessageRetrievalSuccess(message, category, emoji, semaphore),
                throwable -> onReactionProcessingFailure(throwable, semaphore));
    }

    public void approve(DiscordCategory category, String content) {
        mediaRequestService.save(new DiscordMediaRequest(category, content));
        sendingService.updateCategory(category);
    }

    public void addCategory(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Lock lock = new ReentrantLock();
        lock.lock();
        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        if (lockService.putIfAbsent(lockKey, lock) != null) {
            lock.unlock();
            return;
        }

        Long approvalChannelId = category.getApprovalChannelId();
        categories.put(approvalChannelId, categoryId);
        approvalChannels.put(categoryId, approvalChannelId);

        semaphoreService.add(ConcurrencyScope.CATEGORY_APPROVE, categoryId);

        lock.unlock();
    }

    public void removeCategory(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        ConcurrencyKey lockKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(category.getApprovalChannelId());
        approvalChannels.remove(categoryId);

        semaphoreService.remove(ConcurrencyScope.CATEGORY_APPROVE, categoryId);

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void updateCategory(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Lock lock = lockService.get(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        if (lock == null) {
            return;
        }

        lock.lock();

        Long newApprovalChannelId = category.getApprovalChannelId();
        Long oldApprovalChannelId = approvalChannels.put(categoryId, newApprovalChannelId);

        // update approval channel id
        if (!newApprovalChannelId.equals(oldApprovalChannelId)) {
            categories.remove(oldApprovalChannelId);
            categories.put(newApprovalChannelId, categoryId);
        }

        lock.unlock();
    }

    // can usually occur when the connection is lost or the channel is unavailable
    private void onReactionProcessingFailure(Throwable throwable, Semaphore semaphore) {
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
        semaphore.release();
    }

    private void onMessageRetrievalSuccess(Message message, DiscordCategory category, String emoji,
                                           Semaphore semaphore) {
        String positiveEmoji = category.getPositiveApprovalEmoji().toString();
        String negativeEmoji = category.getNegativeApprovalEmoji().toString();

        if (emoji.equals(positiveEmoji)) {
            message.delete().queue(unused -> onMessageDeletionSuccessPositive(message, category, semaphore),
                    throwable -> onReactionProcessingFailure(throwable, semaphore));
        } else if (emoji.equals(negativeEmoji)) {
            message.delete().queue(unused -> onMessageDeletionSuccessNegative(semaphore),
                    throwable -> onReactionProcessingFailure(throwable, semaphore));
        }
    }

    private void onMessageDeletionSuccessPositive(Message message, DiscordCategory category, Semaphore semaphore) {
        // todo suggester info
        approve(category, message.getContentRaw());
        semaphore.release();
    }

    private void onMessageDeletionSuccessNegative(Semaphore semaphore) {
        semaphore.release();
    }
}
