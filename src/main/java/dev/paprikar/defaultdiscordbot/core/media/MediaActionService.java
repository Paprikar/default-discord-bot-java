package dev.paprikar.defaultdiscordbot.core.media;

import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.MediaRequestSender;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class MediaActionService {

    private static final Logger logger = LoggerFactory.getLogger(MediaActionService.class);

    private final DiscordCategoryService categoryService;

    private final DiscordMediaRequestService mediaRequestService;

    private final DiscordProviderFromDiscordService discordProviderService;

    private final DiscordSuggestionService discordSuggestionService;

    private final ApproveService approveService;

    private final SendingService sendingService;

    public MediaActionService(DiscordCategoryService categoryService,
                              DiscordMediaRequestService mediaRequestService,
                              DiscordProviderFromDiscordService discordProviderService,
                              DiscordSuggestionService discordSuggestionService,
                              ApproveService approveService,
                              SendingService sendingService) {
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.discordProviderService = discordProviderService;
        this.discordSuggestionService = discordSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
    }

    public void enableCategory(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<DiscordProviderFromDiscord> discordProviders = discordProviderService
                .findAllByCategoryId(category.getId());
        for (DiscordProviderFromDiscord p : discordProviders) {
            if (p.isEnabled()) {
                enableDiscordProvider(p);
            }
        }

        enableApprove(category);

        enableSending(category, new MediaRequestSender(jda, mediaRequestService, categoryService));

        logger.debug("enableCategory(): category={id={}} is enabled", category.getId());
    }

    public void disableCategory(@Nonnull DiscordCategory category) {
        List<DiscordProviderFromDiscord> discordProviders = discordProviderService
                .findAllByCategoryId(category.getId());
        for (DiscordProviderFromDiscord p : discordProviders) {
            if (p.isEnabled()) {
                disableDiscordProvider(p);
            }
        }

        disableApprove(category);

        disableSending(category);

        logger.debug("disableCategory(): category={id={}} is disabled", category.getId());
    }

    public void enableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        DiscordCategory category = provider.getCategory();
        if (category.isEnabled()) {
            discordSuggestionService.addSuggestionChannel(provider.getSuggestionChannelId(), category.getId());
        }

        logger.debug("enableDiscordProvider(): provider={id={}} is enabled", provider.getId());
    }

    public void disableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        DiscordCategory category = provider.getCategory();
        if (category.isEnabled()) {
            discordSuggestionService.removeSuggestionChannel(provider.getSuggestionChannelId());
        }

        logger.debug("disableDiscordProvider(): provider={id={}} is disabled", provider.getId());
    }

    public void enableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        // todo

        logger.debug("enableVkProvider(): provider={id={}} is enabled", provider.getId());
    }

    public void disableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        // todo

        logger.debug("disableVkProvider(): provider={id={}} is disabled", provider.getId());
    }

    public void enableApprove(@Nonnull DiscordCategory category) {
        approveService.addCategory(category);

        logger.debug("enableApprove(): Media approve for category={id={}} is enabled", category.getId());
    }

    public void disableApprove(@Nonnull DiscordCategory category) {
        approveService.removeCategory(category);

        logger.debug("disableApprove(): Media approve for category={id={}} is disabled", category.getId());
    }

    public void enableSending(@Nonnull DiscordCategory category, @Nonnull MediaRequestSender mediaRequestSender) {
        sendingService.addSender(mediaRequestSender, category);

        logger.debug("enableSending(): Media sending for category={id={}} is enabled", category.getId());
    }

    public void disableSending(@Nonnull DiscordCategory category) {
        sendingService.removeSender(category.getId());

        logger.debug("disableSending(): Media sending for category={id={}} is disabled", category.getId());
    }
}
