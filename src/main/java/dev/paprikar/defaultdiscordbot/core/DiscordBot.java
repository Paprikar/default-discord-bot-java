package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
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

    private final DiscordCategoryService categoryService;

    private final MediaActionService mediaActionService;

    private final DiscordEventListener eventListener;

    private final DdbConfig config;

    public DiscordBot(DiscordCategoryService categoryService,
                      MediaActionService mediaActionService,
                      DiscordEventListener eventListener,
                      DdbConfig config) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
        this.config = config;
        this.eventListener = eventListener;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            JDA jda = JDABuilder.createDefault(config.getToken())
                    .addEventListeners(eventListener)
                    .build()
                    .awaitReady();
            initBot(jda);
            logger.info(jda.getGuilds().toString());
        } catch (LoginException | InterruptedException e) {
            logger.error("An error occurred while starting the Discord bot", e);
            System.exit(1);
        }
    }

    public DdbConfig getConfig() {
        return config;
    }

    private void initBot(JDA jda) {
        categoryService.findAll().stream()
                .filter(DiscordCategory::isEnabled)
                .forEach(category -> mediaActionService.enableCategory(category, jda));
    }
}
