package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SendingService {

    private final Logger logger = LoggerFactory.getLogger(SendingService.class);

    // Map<CategoryId, Sender>
    private final Map<Long, MediaRequestSender> senders = new ConcurrentHashMap<>();

    /**
     * q отдельно для каждой категории
     * category:
     * name                   - ignore
     * sendingChannelId       - ignore
     * approvalChannelId      - ignore
     * startTime              - restart (send time) | update
     * endTime                - restart (send time) | update
     * reserveDays            - restart (send time) | update
     * positiveApprovalEmoji  - ignore
     * negativeApprovalEmoji  - ignore
     * +enabled               - start | add
     * -enabled               - stop | remove
     * <p>
     * providers - ignore
     */

    public SendingService() {
    }

    public void addSender(@Nonnull MediaRequestSender sender, @Nonnull DiscordCategory category) {
        senders.put(category.getId(), sender);
        sender.start(category);
    }

    public void removeSender(long categoryId) {
        MediaRequestSender sender = senders.remove(categoryId);
        if (sender != null) {
            sender.stop(false);
        }
    }

    public void updateSender(@Nonnull DiscordCategory category) {
        MediaRequestSender sender = senders.get(category.getId());
        if (sender == null) {
            String message = "No sender found for the corresponding category";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        sender.stop(true);
        sender.start(category);
    }
}
