package dev.paprikar.defaultdiscordbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

public class JDAService {

    private static JDA jda = null;

    public static JDA build(@Nonnull String token, Integer maxReconnectDelay, DiscordEventListener eventListener)
            throws LoginException {
        if (jda != null) {
            return jda;
        }

        JDABuilder builder = JDABuilder.createDefault(token);

        if (maxReconnectDelay != null) {
            builder.setMaxReconnectDelay(maxReconnectDelay);
        }

        if (eventListener != null) {
            builder.addEventListeners(eventListener);
        }

        jda = builder.build();
        return jda;
    }

    @Nullable
    public static JDA get() {
        return jda;
    }
}
