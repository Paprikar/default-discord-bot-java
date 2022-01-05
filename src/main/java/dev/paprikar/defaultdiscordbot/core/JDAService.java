package dev.paprikar.defaultdiscordbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public class JDAService {

    private JDA jda = null;

    public JDA build(String token, Integer maxReconnectDelay, DiscordEventListener eventListener)
            throws LoginException {
        jda = JDABuilder.createDefault(token)
                .addEventListeners(eventListener)
                .setMaxReconnectDelay(maxReconnectDelay)
                .build();
        return jda;
    }

    public JDA get() {
        return jda;
    }
}
