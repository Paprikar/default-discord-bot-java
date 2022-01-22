package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.messages.Message;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.paprikar.defaultdiscordbot.utils.VkUtils.executeRequest;

public class GroupLongPollApiHandler extends GroupLongPollApi {

    private static final Logger logger = LoggerFactory.getLogger(GroupLongPollApiHandler.class);

    private final VkSuggestionHandler suggestionHandler;

    public GroupLongPollApiHandler(GroupActor actor,
                                   int maxReconnectDelay,
                                   VkSuggestionService suggestionService,
                                   VkSuggestionHandler suggestionHandler,
                                   LockService lockService) {
        super(actor, maxReconnectDelay, suggestionService, lockService);

        this.suggestionHandler = suggestionHandler;
    }

    public GroupLongPollApiHandler(GroupActor actor,
                                   int maxReconnectDelay,
                                   int waitTime,
                                   VkSuggestionService suggestionService,
                                   VkSuggestionHandler suggestionHandler,
                                   LockService lockService) {
        super(actor, maxReconnectDelay, waitTime, suggestionService, lockService);

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
