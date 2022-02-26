package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.events.EventsHandler;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiGroupAuthException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.objects.callback.messages.CallbackMessage;
import com.vk.api.sdk.objects.groups.LongPollServer;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Custom implementation of vk's {@link com.vk.api.sdk.events.longpoll.GroupLongPollApi GroupLongPollApi}.
 * <p>
 * Does not react to events after stopping. Allows to reuse a previously
 * stopped handler, taking into account possible changes in credentials.
 */
class GroupLongPollApi extends EventsHandler {

    /**
     * An instance of {@link VkApiClient}.
     */
    protected static final VkApiClient client = VkSuggestionService.CLIENT;

    private static final Logger logger = LoggerFactory.getLogger(GroupLongPollApi.class);

    private static final int DEFAULT_WAIT_TIME = 25;

    /**
     * The {@link GroupActor} used by this handler.
     */
    protected final GroupActor actor;

    private final VkSuggestionService suggestionService;

    private final MonitorService monitorService;

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

    /**
     * Constructs the handler.
     *
     * @param actor
     *         the {@link GroupActor}
     * @param maxReconnectDelay
     *         the maximum reconnection delay of the handler in seconds
     * @param suggestionService
     *         an instance of {@link VkSuggestionService}
     * @param monitorService
     *         an instance of {@link MonitorService}
     */
    public GroupLongPollApi(GroupActor actor,
                            int maxReconnectDelay,
                            VkSuggestionService suggestionService,
                            MonitorService monitorService) {
        this(actor, maxReconnectDelay, DEFAULT_WAIT_TIME, suggestionService, monitorService);
    }

    /**
     * Constructs the handler.
     *
     * @param actor
     *         the {@link GroupActor}
     * @param maxReconnectDelay
     *         the maximum reconnection delay of the handler in seconds
     * @param waitTime
     *         the time for which the event request connection is kept. After it expires,
     *         the connection will be closed and the handler will try to create a new request
     * @param suggestionService
     *         an instance of {@link VkSuggestionService}
     * @param monitorService
     *         an instance of {@link MonitorService}
     */
    public GroupLongPollApi(GroupActor actor,
                            int maxReconnectDelay,
                            int waitTime,
                            VkSuggestionService suggestionService,
                            MonitorService monitorService) {
        this.actor = actor;
        this.maxReconnectDelay = maxReconnectDelay;
        this.waitTime = waitTime;
        this.suggestionService = suggestionService;
        this.monitorService = monitorService;
    }

    /**
     * Should the handler run?
     *
     * @return {@code true} if the handler should run, otherwise {@code false}
     */
    public boolean isToRun() {
        return toRun;
    }

    /**
     * @return the vk provider that is used by this handler
     */
    public DiscordProviderFromVk getProvider() {
        return provider;
    }

    /**
     * Returns the cached vk provider that is used by this handler.
     * <p>
     * Used to optimize access to the vk provider, bypassing the forced lookup to shared memory.
     *
     * @return the cached vk provider that is used by this handler
     */
    protected DiscordProviderFromVk getProviderCached() {
        return providerCached;
    }

    /**
     * Starts the handler.
     *
     * @param provider
     *         the vk provider
     *
     * @throws IllegalStateException
     *         if the handler is already started
     */
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

    /**
     * Stops the handler.
     */
    public void stop() {
        toRun = false;

        if (taskFuture != null) { // if reconnection was delayed and scheduled
            taskFuture.cancel(false);
            if (taskFuture.isCancelled()) { // if reconnection was cancelled before attempted
                onStop();
            }
        }
    }

    /**
     * Updates the handler.
     *
     * @param provider
     *         the vk provider
     */
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

        ConcurrencyKey monitorKey = ConcurrencyKey
                .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
        monitorService.remove(monitorKey);
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
            ConcurrencyKey monitorKey = ConcurrencyKey
                    .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
            Object monitor = monitorService.get(monitorKey);
            if (monitor == null) {
                return false;
            }

            synchronized (monitor) {
                boolean toRun = GroupLongPollApi.this.toRun;
                if (!toRun) {
                    onStop();
                }
                return toRun;
            }
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

                // stop if creds is no longer valid
                if (e instanceof ApiGroupAuthException) {
                    ConcurrencyKey monitorKey = ConcurrencyKey
                            .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
                    Object monitor = monitorService.get(monitorKey);
                    if (monitor == null) {
                        return null;
                    }

                    synchronized (monitor) {
                        toRun = false;
                        onStop();
                        return null;
                    }
                }

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
            ConcurrencyKey monitorKey = ConcurrencyKey
                    .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
            Object monitor = monitorService.get(monitorKey);
            if (monitor == null) {
                return;
            }

            synchronized (monitor) {
                if (!toRun) {
                    onStop();
                    return;
                }

                int delay = getDelay();

                logger.debug("UpdaterTask#scheduleTask(): provider={id={}}. " +
                        "Attempting to reconnect in {}s", providerId, delay);

                taskFuture = executor.schedule(new UpdaterTask(), delay, TimeUnit.SECONDS);
            }
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

            ConcurrencyKey monitorKey = ConcurrencyKey
                    .from(ConcurrencyScope.CATEGORY_PROVIDER_FROM_VK_CONFIGURATION, providerId);
            Object monitor = monitorService.get(monitorKey);
            if (monitor == null) {
                return true;
            }

            synchronized (monitor) {
                if (!toRun) {
                    onStop();
                    return true;
                }

                updates.forEach(update -> {
                    try {
                        parse(gson.fromJson(update, CallbackMessage.class));
                    } catch (RuntimeException e) {
                        logger.error("UpdaterTask#parseUpdates(): provider={id={}}. " +
                                "An error occurred while parsing the update", providerId, e);
                    }
                });

                return false;
            }
        }
    }
}
