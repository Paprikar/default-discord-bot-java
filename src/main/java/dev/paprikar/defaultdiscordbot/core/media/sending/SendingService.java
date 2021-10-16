package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
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
public class SendingService {

    private final Logger logger = LoggerFactory.getLogger(SendingService.class);

    private final DiscordGuildService guildService;

    private final ReadWriteLockService readWriteLockService;

    // Map<CategoryId, Sender>
    private final Map<Long, MediaRequestSender> senders = new ConcurrentHashMap<>();

    // Map<SendingChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    public SendingService(DiscordGuildService guildService,
                          ReadWriteLockService readWriteLockService) {
        this.guildService = guildService;
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

        Long categoryId = categories.remove(event.getChannel().getIdLong());
        if (categoryId != null) {
            senders.remove(categoryId).stop();
        }

        writeLock.unlock();
    }

    public void addSender(@Nonnull MediaRequestSender sender, @Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        senders.put(categoryId, sender);
        categories.put(category.getSendingChannelId(), categoryId);
        sender.start(category);
    }

    public void removeSender(long categoryId) {
        MediaRequestSender sender = senders.remove(categoryId);
        if (sender == null) {
            return;
        }
        DiscordCategory category = sender.getCategory();
        if (category == null) {
            // todo throw ?
            return;
        }
        sender.stop();
        categories.remove(category.getSendingChannelId());
    }

    public void updateSender(@Nonnull DiscordCategory category) {
        MediaRequestSender sender = senders.get(category.getId());
        if (sender == null) {
            String message = "No sender found for the corresponding category";
            logger.error(message);
            return;
        }
        sender.refresh(category);
    }
}
