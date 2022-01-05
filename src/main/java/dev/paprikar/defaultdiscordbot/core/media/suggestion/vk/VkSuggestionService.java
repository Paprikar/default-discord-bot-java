package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class VkSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(VkSuggestionService.class);

    // Map<ProviderId, Handler>
    final Map<Long, GroupLongPollApi> handlers = new ConcurrentHashMap<>();

    private final ApproveService approveService;

    private final LockService lockService;

    private final DdbConfig config;

    private final VkApiClient client = new VkApiClient(HttpTransportClient.getInstance());

    @Autowired
    public VkSuggestionService(ApproveService approveService, LockService lockService, DdbConfig config) {
        this.approveService = approveService;
        this.lockService = lockService;
        this.config = config;
    }

    public VkApiClient getClient() {
        return client;
    }

    public void add(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();
        Lock newLock = new ReentrantLock();
        newLock.lock();
        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        Lock oldLock = lockService.putIfAbsent(lockKey, newLock);
        if (oldLock != null) { // try to reuse the handler before its stopped
            newLock.unlock();
            oldLock.lock();

            GroupLongPollApi handler = handlers.get(providerId);

            if (handler != null) { // there are only unstopped handlers in the map
                handler.start(provider);

                oldLock.unlock();
                return;
            }

            oldLock.unlock();
        }

        // create a new handler when there was none or when the old one cannot be reused
        GroupActor actor = new GroupActor(provider.getGroupId(), provider.getToken());
        GroupLongPollApi handler = new GroupLongPollApiHandler(
                client, actor, config.getVkMaxReconnectDelay(), this, approveService, lockService);

        handlers.put(providerId, handler);

        handler.start(provider);

        newLock.unlock();
    }

    public void remove(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();
        ConcurrencyKey lockKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        Lock lock = lockService.get(lockKey);
        if (lock == null) {
            return;
        }

        lock.lock();

        GroupLongPollApi handler = handlers.get(providerId);

        if (handler != null) {
            handler.stop();
        }

        lock.unlock();
    }

    public void update(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();
        Lock lock = lockService.get(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        if (lock == null) {
            return;
        }

        lock.lock();

        GroupLongPollApi handler = handlers.get(providerId);

        if (handler == null) {
            lock.unlock();
            return;
        }

        if (!Objects.equals(handler.getProvider(), provider)) {
            lock.unlock();

            String message = "Provider update is possible, but not a replacement";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        handler.update(provider);

        lock.unlock();
    }
}
