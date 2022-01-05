package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SendingService {

    private static final Logger logger = LoggerFactory.getLogger(SendingService.class);

    private final LockService lockService;

    // Map<CategoryId, Sender>
    private final Map<Long, MediaRequestSender> senders = new ConcurrentHashMap<>();

    // Map<SendingChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    @Autowired
    public SendingService(LockService lockService) {
        this.lockService = lockService;
    }

    public void handleTextChannelDeleteEvent(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();
        Long categoryId = categories.get(channelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey lockKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        categories.remove(channelId);

        MediaRequestSender sender = senders.remove(categoryId);
        if (sender != null) {
            sender.stop();
        }

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void add(@Nonnull DiscordCategory category, @Nonnull MediaRequestSender sender) {
        Long categoryId = category.getId();
        Lock lock = new ReentrantLock();
        lock.lock();
        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        if (lockService.putIfAbsent(lockKey, lock) != null) {
            lock.unlock();
            return;
        }

        senders.put(categoryId, sender);
        categories.put(category.getSendingChannelId(), categoryId);

        sender.start(category);

        lock.unlock();
    }

    public void remove(long categoryId) {
        ConcurrencyKey lockKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        MediaRequestSender sender = senders.remove(categoryId);
        if (sender == null) {
            lock.unlock();
            return;
        }

        DiscordCategory category = sender.getCategory();
        sender.stop();

        if (category != null) {
            categories.remove(category.getSendingChannelId());
        }

        lockService.remove(lockKey);

        lock.unlock();
    }

    public void update(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Lock lock = lockService.get(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        if (lock == null) {
            return;
        }

        lock.lock();

        MediaRequestSender sender = senders.get(categoryId);
        if (sender == null) {
            String message = "No sender found for the corresponding category";
            logger.error(message);
            lock.unlock();
            return;
        }

        // update sending channel id
        DiscordCategory oldCategory = sender.getCategory();
        if (oldCategory != null) {
            Long oldSendingChannelId = oldCategory.getSendingChannelId();
            Long newSendingChannelId = category.getSendingChannelId();
            if (!oldSendingChannelId.equals(newSendingChannelId)) {
                categories.put(newSendingChannelId, categoryId);
                categories.remove(oldSendingChannelId);
            }
        }

        sender.update(category);

        lock.unlock();
    }
}
