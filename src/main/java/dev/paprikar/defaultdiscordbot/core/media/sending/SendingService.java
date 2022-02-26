package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest.DiscordMediaRequestService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for sending category suggestions.
 */
@Service
public class SendingService {

    private static final Logger logger = LoggerFactory.getLogger(SendingService.class);

    private final DiscordCategoryService categoryService;
    private final DiscordMediaRequestService mediaRequestService;
    private final MonitorService monitorService;

    // Map<CategoryId, Sender>
    private final Map<Long, MediaRequestSender> senders = new ConcurrentHashMap<>();

    // Map<SendingChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    /**
     * Constructs a sending service.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param mediaRequestService
     *         an instance of {@link DiscordMediaRequestService}
     * @param monitorService
     *         an instance of {@link MonitorService}
     */
    @Autowired
    public SendingService(DiscordCategoryService categoryService,
                          DiscordMediaRequestService mediaRequestService,
                          MonitorService monitorService) {
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.monitorService = monitorService;
    }

    /**
     * Handles events of type {@link TextChannelDeleteEvent}.
     *
     * @param event
     *         the event of type {@link TextChannelDeleteEvent} for handling
     */
    public void handleTextChannelDeleteEvent(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();
        Long categoryId = categories.get(channelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            MediaRequestSender sender = senders.remove(categoryId);
            if (sender != null) {
                sender.stop();
            }

            categories.remove(channelId);

            monitorService.remove(monitorKey);
        }

        logger.debug("handleTextChannelDeleteEvent(): Media sending for category={id={}} is disabled "
                + "due to the deletion of the required text channel with id={}", categoryId, channelId);
    }

    /**
     * Adds the category to sending processing context.
     *
     * @param category
     *         the category
     * @param jda
     *         an instance of {@link JDA}
     */
    public void add(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        Long categoryId = category.getId();

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        Object monitor = new Object();

        synchronized (monitor) {
            if (monitorService.putIfAbsent(monitorKey, monitor) != null) {
                return;
            }

            MediaRequestSender sender = new MediaRequestSender(jda, categoryService, mediaRequestService,
                    monitorService);

            senders.put(categoryId, sender);
            categories.put(category.getSendingChannelId(), categoryId);

            sender.start(category);
        }
    }

    /**
     * Removes the category from sending processing context.
     *
     * @param category
     *         the category
     */
    public void remove(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            MediaRequestSender sender = senders.remove(categoryId);
            if (sender != null) {
                sender.stop();
            }

            categories.remove(category.getSendingChannelId());

            monitorService.remove(monitorKey);
        }
    }

    /**
     * Updates the category in sending processing context.
     *
     * @param category
     *         the category
     */
    public void update(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        Object monitor = monitorService.get(ConcurrencyScope.CATEGORY_SENDING_CONFIGURATION, categoryId);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            MediaRequestSender sender = senders.get(categoryId);
            if (sender == null) {
                logger.error("No sender found for the corresponding category");
                return;
            }

            // update sending channel id
            DiscordCategory oldCategory = sender.getCategory();
            if (oldCategory != null) {
                Long oldSendingChannelId = oldCategory.getSendingChannelId();
                Long newSendingChannelId = category.getSendingChannelId();
                if (!Objects.equals(oldSendingChannelId, newSendingChannelId)) {
                    categories.remove(oldSendingChannelId);
                    categories.put(newSendingChannelId, categoryId);
                }
            }

            sender.update(category);
        }
    }

    /**
     * Does the category exists in sending processing context?
     *
     * @param category
     *         the category
     *
     * @return {@code true} if the category exists in sending processing context
     */
    public boolean contains(@Nonnull DiscordCategory category) {
        return senders.containsKey(category.getId());
    }
}
