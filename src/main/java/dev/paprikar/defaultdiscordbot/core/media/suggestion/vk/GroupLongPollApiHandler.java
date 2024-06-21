package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.messages.Message;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.paprikar.defaultdiscordbot.utils.VkRequests.executeRequest;

/**
 * Handles vk group long poll api events.
 */
public class GroupLongPollApiHandler extends GroupLongPollApi {

    private static final Logger logger = LoggerFactory.getLogger(GroupLongPollApiHandler.class);

    private final VkSuggestionHandler suggestionHandler;

    /**
     * Constructs the handler.
     *
     * @param actor the {@link GroupActor}
     * @param maxReconnectDelay the maximum reconnection delay of the handler in seconds
     * @param suggestionService an instance of {@link VkSuggestionService}
     * @param suggestionHandler an instance of {@link VkSuggestionHandler}
     * @param monitorService an instance of {@link MonitorService}
     */
    public GroupLongPollApiHandler(GroupActor actor,
                                   int maxReconnectDelay,
                                   VkSuggestionService suggestionService,
                                   VkSuggestionHandler suggestionHandler,
                                   MonitorService monitorService) {
        super(actor, maxReconnectDelay, suggestionService, monitorService);

        this.suggestionHandler = suggestionHandler;
    }

    /**
     * Constructs the handler.
     *
     * @param actor the {@link GroupActor}
     * @param maxReconnectDelay the maximum reconnection delay of the handler in seconds
     * @param waitTime the time for which the event request connection is kept. After it expires,
     * the connection will be closed and the handler will try to create a new request
     * @param suggestionService an instance of {@link VkSuggestionService}
     * @param suggestionHandler an instance of {@link VkSuggestionHandler}
     * @param monitorService an instance of {@link MonitorService}
     */
    public GroupLongPollApiHandler(GroupActor actor,
                                   int maxReconnectDelay,
                                   int waitTime,
                                   VkSuggestionService suggestionService,
                                   VkSuggestionHandler suggestionHandler,
                                   MonitorService monitorService) {
        super(actor, maxReconnectDelay, waitTime, suggestionService, monitorService);

        this.suggestionHandler = suggestionHandler;
    }

    @Override
    protected void messageNew(Integer groupId, Message message) {
        logger.debug("messageNew(): groupId={}, message={}", groupId, message);

        executeRequest(client.messages().markAsRead(actor)
                .peerId(message.getPeerId()));

        suggestionHandler.handleMessageNewEvent(message, actor, getProviderCached());
    }
}
