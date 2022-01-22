package dev.paprikar.defaultdiscordbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

@Service
public class JDAService {

    private JDA jda = null;

    public JDA build(@Nonnull String token, Integer maxReconnectDelay, DiscordEventListener eventListener)
            throws LoginException {
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
    public JDA get() {
        return jda;
    }
}
