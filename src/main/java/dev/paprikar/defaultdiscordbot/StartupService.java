package dev.paprikar.defaultdiscordbot;

import dev.paprikar.defaultdiscordbot.core.media.MediaAction;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class StartupService {

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromDiscordService discordProviderService;

    private final DiscordSuggestionService discordSuggestionService;

    public StartupService(DiscordCategoryService categoryService,
                          DiscordProviderFromDiscordService discordProviderService,
                          DiscordSuggestionService discordSuggestionService) {
        this.categoryService = categoryService;
        this.discordProviderService = discordProviderService;
        this.discordSuggestionService = discordSuggestionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        List<DiscordCategory> categories = categoryService.findAll();
        for (DiscordCategory c : categories) {
            if (c.isEnabled()) {
                MediaAction.enableCategory(c, discordProviderService, discordSuggestionService);
            }
        }
    }
}
