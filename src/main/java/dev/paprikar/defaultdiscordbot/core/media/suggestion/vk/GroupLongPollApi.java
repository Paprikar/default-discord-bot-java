package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.events.EventsHandler;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.objects.callback.messages.CallbackMessage;
import com.vk.api.sdk.objects.groups.LongPollServer;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

class GroupLongPollApi extends EventsHandler {

    protected static final VkApiClient client = VkSuggestionService.CLIENT;

    private static final Logger logger = LoggerFactory.getLogger(GroupLongPollApi.class);

    private static final int DEFAULT_WAIT_TIME = 25;

    protected final VkSuggestionService suggestionService;

    protected final GroupActor actor;

    private final LockService lockService;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final int maxReconnectDelay; // in seconds

    private final int waitTime; // in seconds

    private volatile ScheduledFuture<?> taskFuture;

    private volatile boolean toRun = false;

    private volatile boolean isStopped = true;

    private volatile DiscordProviderFromVk provider;

    private Long providerId = null;

    private DiscordProviderFromVk providerCached = null;

    private int lastReconnectDelay = 1; // in seconds

    public GroupLongPollApi(GroupActor actor,
                            int maxReconnectDelay,
                            VkSuggestionService suggestionService,
                            LockService lockService) {
        this(actor, maxReconnectDelay, DEFAULT_WAIT_TIME, suggestionService, lockService);
    }

    public GroupLongPollApi(GroupActor actor,
                            int maxReconnectDelay,
                            int waitTime,
                            VkSuggestionService suggestionService,
                            LockService lockService) {
        this.actor = actor;
        this.maxReconnectDelay = maxReconnectDelay;
        this.waitTime = waitTime;
        this.suggestionService = suggestionService;
        this.lockService = lockService;
    }

    public boolean isToRun() {
        return toRun;
    }

    public DiscordProviderFromVk getProvider() {
        return provider;
    }

    protected DiscordProviderFromVk getProviderCached() {
        return providerCached;
    }

    public void start(@Nonnull DiscordProviderFromVk provider) {
        providerId = provider.getId();

        if (toRun) {
            String message = "start(): provider={id={" + providerId + "}. Long Poll handler is already started";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        toRun = true;
        this.provider = provider;

        if (!isStopped) {
            return;
        }

        logger.debug("start(): provider={id={}}. Long Poll handler started to handle events", providerId);

        taskFuture = null;
        isStopped = false;

        executor.execute(new UpdaterTask());
    }

    public void stop() {
        toRun = false;

        if (taskFuture != null) { // if reconnection was delayed and scheduled
            taskFuture.cancel(false);
            if (taskFuture.isCancelled()) { // if reconnection was cancelled before attempted
                onStop();
            }
        }
    }

    public void update(@Nonnull DiscordProviderFromVk provider) {
        this.provider = provider;
    }

    private void onStop() {
        logger.debug("onStop(): provider={id={}}. Long Poll handler stopped to handle events",
                providerId);

        isStopped = true;
        provider = null;

        executor.shutdown();

        suggestionService.handlers.remove(providerId);

        lockService.remove(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
    }

    private class UpdaterTask implements Runnable {

        public UpdaterTask() {
        }

        @Override
        public void run() {
            handleUpdates();
        }

        private void handleUpdates() {
            LongPollServer lpServer = getLongPollServer();
            if (lpServer == null) {
                return;
            }
            String ts = lpServer.getTs();
            GetLongPollEventsResponse eventsResponse;

            while (tryToRun()) {
                try {
                    eventsResponse = client.longPoll()
                            .getEvents(lpServer.getServer(), lpServer.getKey(), ts)
                            .waitTime(waitTime)
                            .execute();
                    boolean toStop = parseUpdates(eventsResponse.getUpdates());
                    if (toStop) {
                        return;
                    }
                    ts = eventsResponse.getTs();
                } catch (ApiException | ClientException e) {
                    logger.debug("UpdaterTask#handleUpdates(): provider={id={}}. " +
                            "An error occurred while running the Long Poll handler", providerId, e);
                    lpServer = getLongPollServer();
                    if (lpServer == null) {
                        return;
                    }
                    ts = lpServer.getTs();
                }
            }
        }

        private boolean tryToRun() {
            ConcurrencyKey lockKey = ConcurrencyKey
                    .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
            Lock lock = lockService.get(lockKey);
            if (lock == null) {
                return false;
            }

            lock.lock();

            boolean toRun = GroupLongPollApi.this.toRun;
            if (!toRun) {
                onStop();
            }

            lock.unlock();

            return toRun;
        }

        private LongPollServer getLongPollServer() {
            try {
                GetLongPollServerResponse response = client.groupsLongPoll()
                        .getLongPollServer(actor, actor.getGroupId())
                        .execute();
                LongPollServer lpServer = initServer(response);

                lastReconnectDelay = 1;

                return lpServer;
            } catch (ApiException | ClientException e) {
                logger.debug("UpdaterTask#getLongPollServer(): provider={id={}}. " +
                        "An error occurred while getting the Long Poll server", providerId, e);

                scheduleTask();

                return null;
            }
        }

        private LongPollServer initServer(GetLongPollServerResponse lpServerResponse) {
            return new LongPollServer()
                    .setKey(lpServerResponse.getKey())
                    .setTs(lpServerResponse.getTs())
                    .setServer(lpServerResponse.getServer());
        }

        private void scheduleTask() {
            ConcurrencyKey lockKey = ConcurrencyKey
                    .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
            Lock lock = lockService.get(lockKey);
            if (lock == null) {
                return;
            }

            lock.lock();

            if (!toRun) {
                onStop();

                lock.unlock();
                return;
            }

            int delay = getDelay();

            logger.debug("UpdaterTask#scheduleTask(): provider={id={}}. " +
                    "Attempting to reconnect in {}s", providerId, delay);

            taskFuture = executor.schedule(new UpdaterTask(), delay, TimeUnit.SECONDS);

            lock.unlock();
        }

        private int getDelay() {
            int delay = Math.min(lastReconnectDelay * 2, maxReconnectDelay);
            lastReconnectDelay = delay;
            return delay;
        }

        /**
         * @return {@code true} if task should be stopped
         */
        private boolean parseUpdates(List<JsonObject> updates) {
            providerCached = provider;
            ConcurrencyKey lockKey = ConcurrencyKey
                    .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
            Lock lock = lockService.get(lockKey);
            if (lock == null) {
                return true;
            }

            lock.lock();

            if (!toRun) {
                onStop();

                lock.unlock();
                return true;
            }

            for (JsonObject update : updates) {
                try {
                    parse(gson.fromJson(update, CallbackMessage.class));
                } catch (RuntimeException e) {
                    logger.error("UpdaterTask#parseUpdates(): provider={id={}}. " +
                            "An error occurred while parsing the update", providerId, e);
                }
            }

            lock.unlock();
            return false;
        }
    }
}
