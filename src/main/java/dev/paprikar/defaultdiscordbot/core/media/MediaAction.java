package dev.paprikar.defaultdiscordbot.core.media;

import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MediaAction {

    private static final Logger logger = LoggerFactory.getLogger(MediaAction.class);

    public static void enableCategory(DiscordCategory category,
                                      DiscordProviderFromDiscordService discordProviderService,
                                      DiscordSuggestionService discordSuggestionService) {
        // todo approve and sending activation


        List<DiscordProviderFromDiscord> discordProviders = discordProviderService
                .findAllByCategoryId(category.getId());
        for (DiscordProviderFromDiscord p : discordProviders) {
            if (p.isEnabled()) {
                enableDiscordProvider(p, discordSuggestionService);
            }
        }

        logger.debug("enableCategory(): category={id={}} is enabled", category.getId());
    }

    public static void disableCategory(DiscordCategory category,
                                       DiscordProviderFromDiscordService discordProviderService,
                                       DiscordSuggestionService discordSuggestionService) {
        // todo approve and sending deactivation
        List<DiscordProviderFromDiscord> discordProviders = discordProviderService
                .findAllByCategoryId(category.getId());
        for (DiscordProviderFromDiscord p : discordProviders) {
            if (p.isEnabled()) {
                disableDiscordProvider(p, discordSuggestionService);
            }
        }
        logger.debug("disableCategory(): category={id={}} is disabled", category.getId());
    }

    public static void enableDiscordProvider(DiscordProviderFromDiscord provider,
                                             DiscordSuggestionService discordSuggestionService) {
        DiscordCategory category = provider.getCategory();
        if (category.isEnabled()) {
            discordSuggestionService.addSuggestionChannel(provider.getSuggestionChannelId(), category.getId());
        }
        logger.debug("enableDiscordProvider(): provider={id={}} is enabled", provider.getId());
    }

    public static void disableDiscordProvider(DiscordProviderFromDiscord provider,
                                              DiscordSuggestionService discordSuggestionService) {
        DiscordCategory category = provider.getCategory();
        if (category.isEnabled()) {
            discordSuggestionService.removeSuggestionChannel(provider.getSuggestionChannelId());
        }
        logger.debug("disableDiscordProvider(): provider={id={}} is disabled", provider.getId());
    }

    public static void enableVkProvider(DiscordProviderFromVk provider,
                                        VkSuggestionService vkSuggestionService) {
        // todo
        logger.debug("enableVkProvider(): provider={id={}} is enabled", provider.getId());
    }

    public static void disableVkProvider(DiscordProviderFromVk provider,
                                         VkSuggestionService vkSuggestionService) {
        // todo
        logger.debug("disableVkProvider(): provider={id={}} is disabled", provider.getId());
    }
}
