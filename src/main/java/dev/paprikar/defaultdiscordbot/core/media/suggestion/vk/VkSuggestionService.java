package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for vk suggestions.
 */
@Service
public class VkSuggestionService {

    /**
     * An instance of {@link VkApiClient}.
     */
    public static final VkApiClient CLIENT = new VkApiClient(HttpTransportClient.getInstance());

    private static final Logger logger = LoggerFactory.getLogger(VkSuggestionService.class);

    /**
     * Map to get handlers by corresponding providers ids.
     */
    // Map<ProviderId, Handler>
    final Map<Long, GroupLongPollApi> handlers = new ConcurrentHashMap<>();

    private final VkSuggestionHandler suggestionHandler;
    private final MonitorService monitorService;
    private final DdbConfig config;

    /**
     * Constructs the service.
     *
     * @param suggestionHandler
     *         an instance of {@link VkSuggestionHandler}
     * @param monitorService
     *         an instance of {@link MonitorService}
     * @param config
     *         an instance of {@link DdbConfig}
     */
    @Autowired
    public VkSuggestionService(VkSuggestionHandler suggestionHandler,
                               MonitorService monitorService,
                               DdbConfig config) {
        this.suggestionHandler = suggestionHandler;
        this.monitorService = monitorService;
        this.config = config;
    }

    /**
     * Adds the vk provider to suggestion processing context.
     *
     * @param provider
     *         the vk provider
     */
    public void add(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();
        Integer groupId = provider.getGroupId();
        String token = provider.getToken();
        Integer vkMaxReconnectDelay = config.getVkMaxReconnectDelay();

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        Object newMonitor = new Object();

        synchronized (newMonitor) {
            Object oldMonitor = monitorService.putIfAbsent(monitorKey, newMonitor);
            if (oldMonitor != null) {
                // try to reuse the handler before its stopped
                synchronized (oldMonitor) {
                    GroupLongPollApi handler = handlers.get(providerId);
                    if (handler != null) {
                        // there are only unstopped handlers in the map
                        if (handler.isToRun()) {
                            // idempotency
                            logger.debug("add(): Provider is already added. Skipping");
                        } else {
                            handler.start(provider);
                        }
                        return;
                    }
                }
            }

            // create a new handler when there was none or when the old one cannot be reused
            GroupActor actor = new GroupActor(groupId, token);
            GroupLongPollApi handler = new GroupLongPollApiHandler(
                    actor, vkMaxReconnectDelay, this, suggestionHandler, monitorService);

            handlers.put(providerId, handler);

            handler.start(provider);
        }
    }

    /**
     * Removes the vk provider from suggestion processing context.
     *
     * @param provider
     *         the vk provider
     */
    public void remove(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();

        Object monitor = monitorService.get(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        if (monitor == null) {
            logger.debug("remove(): Provider is already removed. Skipping");
            return;
        }

        synchronized (monitor) {
            GroupLongPollApi handler = handlers.get(providerId);
            if (handler == null) {
                logger.debug("remove(): Provider is already removed. Skipping");
                return;
            }

            handler.stop();
        }
    }

    /**
     * Updates the vk provider in suggestion processing context.
     *
     * @param provider
     *         the vk provider
     */
    public void update(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();

        Object monitor = monitorService.get(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            GroupLongPollApi handler = handlers.get(providerId);

            if (handler == null) {
                return;
            }

            if (!Objects.equals(handler.getProvider().getId(), provider.getId())) {
                String message = "Provider update is possible, but not a replacement";
                logger.error(message);
                throw new IllegalStateException(message);
            }

            handler.update(provider);
        }
    }

    /**
     * Does the vk provider exists in suggestion processing context?
     *
     * @param provider
     *         the vk provider
     *
     * @return {@code true} if the vk provider exists in suggestion processing context
     */
    public boolean contains(@Nonnull DiscordProviderFromVk provider) {
        return handlers.containsKey(provider.getId());
    }
}
