package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.List;

@Component
public class DiscordBot {

    private final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final DiscordGuildService guildService;

    private final DiscordCategoryService categoryService;

    private final MediaActionService mediaActionService;

    private final ReadWriteLockService readWriteLockService;

    private final DiscordEventListener eventListener;

    private final DdbConfig config;

    private JDA jda;

    public DiscordBot(DiscordGuildService guildService,
                      DiscordCategoryService categoryService,
                      MediaActionService mediaActionService,
                      ReadWriteLockService readWriteLockService,
                      DiscordEventListener eventListener,
                      DdbConfig config) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
        this.readWriteLockService = readWriteLockService;
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
            initBot(jda);
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

    private void initBot(JDA jda) {
        List<DiscordGuild> guilds = guildService.findAll();
        for (DiscordGuild guild : guilds) {
            readWriteLockService.add(ReadWriteLockScope.GUILD_CONFIGURATION, guild.getId());
        }

        List<DiscordCategory> categories = categoryService.findAll();
        for (DiscordCategory category : categories) {
            if (category.isEnabled()) {
                mediaActionService.enableCategory(category, jda);
            }
        }
    }
}
