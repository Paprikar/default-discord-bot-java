package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
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

    private final DiscordCategoryService categoryService;
    private final MediaActionService mediaActionService;
    private final DiscordEventListener eventListener;
    private final DdbConfig config;

    /**
     * Constructs the component.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param mediaActionService
     *         an instance of {@link MediaActionService}
     * @param eventListener
     *         an instance of {@link DiscordEventListener}
     * @param config
     *         an instance of {@link DdbConfig}
     */
    @Autowired
    public DiscordBot(DiscordCategoryService categoryService,
                      MediaActionService mediaActionService,
                      DiscordEventListener eventListener,
                      DdbConfig config) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
        this.eventListener = eventListener;
        this.config = config;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        try {
            JDAService
                    .build(config.getToken(), config.getDiscordEventPoolSize(), config.getDiscordMaxReconnectDelay(),
                            eventListener)
                    .awaitReady();
            initBot();
        } catch (LoginException | InterruptedException e) {
            logger.error("An error occurred while starting the Discord bot", e);
            System.exit(1);
        }
    }

    private void initBot() {
        categoryService.findAll().stream()
                .filter(DiscordCategory::isEnabled)
                .forEach(mediaActionService::enableCategory);
    }
}
