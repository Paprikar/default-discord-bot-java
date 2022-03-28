package dev.paprikar.defaultdiscordbot.core.session;

import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract class for private session managing services.
 */
public abstract class DiscordPrivateSession {

    /**
     * A set of discord user id's with an active private session.
     */
    // Set<UserId>
    protected static final Set<Long> activeUsers = ConcurrentHashMap.newKeySet();

    /**
     * General error handler for jda requests.
     */
    protected static final RequestErrorHandler executionErrorHandler = RequestErrorHandler.createBuilder()
            .setMessage("An error occurred while executing the JDA request")
            .build();
}
