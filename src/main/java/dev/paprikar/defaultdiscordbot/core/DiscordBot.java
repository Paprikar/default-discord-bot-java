package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

/**
 * Component for launching a discord bot.
 */
@Component
public class DiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final DiscordBotService discordBotService;
    private final DiscordEventListener eventListener;
    private final DdbConfig config;

    /**
     * Constructs the component.
     *
     * @param discordBotService
     *         an instance of {@link DiscordBotService}
     * @param eventListener
     *         an instance of {@link DiscordEventListener}
     * @param config
     *         an instance of {@link DdbConfig}
     */
    @Autowired
    public DiscordBot(DiscordBotService discordBotService,
                      DiscordEventListener eventListener,
                      DdbConfig config) {
        this.discordBotService = discordBotService;
        this.eventListener = eventListener;
        this.config = config;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        try {
            JDA jda = JDAService.build(config.getToken(), config.getDiscordEventPoolSize(),
                    config.getDiscordMaxReconnectDelay(), eventListener);
            jda.awaitReady();

            discordBotService.initialize(jda);
        } catch (LoginException | InterruptedException e) {
            logger.error("An error occurred while starting the Discord bot", e);
            System.exit(1);
        }
    }
}
