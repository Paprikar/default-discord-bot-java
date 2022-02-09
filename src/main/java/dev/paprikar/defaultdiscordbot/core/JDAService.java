package dev.paprikar.defaultdiscordbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;

public class JDAService {

    private static final CustomizableThreadFactory eventPoolThreadFactory = new CustomizableThreadFactory();

    private static JDA jda = null;

    static {
        eventPoolThreadFactory.setThreadNamePrefix("JDA-EventPool-");
    }

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

    @Nullable
    public static JDA get() {
        return jda;
    }
}
