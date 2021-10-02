package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.core.media.MediaAction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

@Component
public class DiscordBot {

    private final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final DdbConfig config;

    private final DiscordEventListener eventListener;

    private JDA jda;

    public DiscordBot(DdbConfig config,
                      DiscordEventListener eventListener) {
        this.config = config;
        this.eventListener = eventListener;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            jda = JDABuilder.createDefault(config.getToken())
                    .addEventListeners(eventListener)
                    .build()
                    .awaitReady();
            logger.info(jda.getGuilds().toString());
        } catch (LoginException | InterruptedException e) {
            logger.error("An error occurred while starting the Discord bot", e);
            System.exit(1);
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public DdbConfig getConfig() {
        return config;
    }
}
