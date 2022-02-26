package dev.paprikar.defaultdiscordbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;

/**
 * Service for working with an instance of {@link JDA}.
 */
public class JDAService {

    private static final CustomizableThreadFactory eventPoolThreadFactory = new CustomizableThreadFactory();

    private static JDA jda = null;

    static {
        eventPoolThreadFactory.setThreadNamePrefix("JDA-EventPool-");
    }

    /**
     * Builds an instance of {@link JDA}.
     *
     * @param token
     *         the token of the discord bot
     * @param eventPoolSize
     *         the event pool size of the discord bot
     * @param maxReconnectDelay
     *         the maximum reconnection delay of the discord bot in seconds
     * @param eventListener
     *         the event listener of the discord bot
     *
     * @return the built instance of {@link JDA}
     *
     * @throws LoginException
     *         in case of any {@link JDA} instance building errors.
     *         See {@link JDABuilder#build()} for more details
     * @see JDABuilder#build()
     * @see LoginException
     */
    public static JDA build(@Nonnull String token,
                            Integer eventPoolSize,
                            Integer maxReconnectDelay,
                            DiscordEventListener eventListener)
            throws LoginException {
        if (jda != null) {
            return jda;
        }

        JDABuilder builder = JDABuilder.createDefault(token);

        // use custom thread pool instead of executing in the calling thread
        if (eventPoolSize != null && eventPoolSize != 1) {
            if (eventPoolSize == 0) {
                eventPoolSize = Runtime.getRuntime().availableProcessors();
            }
            builder.setEventPool(Executors.newFixedThreadPool(eventPoolSize, eventPoolThreadFactory));
        }

        if (eventListener != null) {
            builder.addEventListeners(eventListener);
        }

        if (maxReconnectDelay != null) {
            builder.setMaxReconnectDelay(maxReconnectDelay);
        }

        jda = builder.build();
        return jda;
    }

    /**
     * Returns the instance of {@link JDA}.
     *
     * @return the instance of {@link JDA}
     */
    @Nullable
    public static JDA get() {
        return jda;
    }
}
