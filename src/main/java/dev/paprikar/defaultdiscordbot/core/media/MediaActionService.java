package dev.paprikar.defaultdiscordbot.core.media;

import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveValidator;
import dev.paprikar.defaultdiscordbot.core.media.sending.MediaRequestSender;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingValidator;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionValidator;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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

    @Autowired
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

    @Nonnull
    public List<MessageEmbed> enableCategory(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = new ArrayList<>();

        errors.addAll(enableSending(category, new MediaRequestSender(jda, mediaRequestService, categoryService), jda));

        errors.addAll(enableApprove(category, jda));

        discordProviderService
                .findAllByCategoryId(category.getId())
                .stream()
                .filter(DiscordProviderFromDiscord::isEnabled)
                .forEach(provider -> errors.addAll(enableDiscordProvider(provider, jda)));

        logger.debug("enableCategory(): category={id={}} is enabled", category.getId());

        return errors;
    }

    public void disableCategory(@Nonnull DiscordCategory category) {
        discordProviderService
                .findAllByCategoryId(category.getId())
                .stream()
                .filter(DiscordProviderFromDiscord::isEnabled)
                .forEach(this::disableDiscordProvider);

        disableApprove(category);

        disableSending(category);

        logger.debug("disableCategory(): category={id={}} is disabled", category.getId());
    }

    @Nonnull
    public List<MessageEmbed> enableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider, @Nonnull JDA jda) {
        List<MessageEmbed> errors = DiscordSuggestionValidator.validateInitially(provider);
        if (!errors.isEmpty()) {
            logger.debug("enableDiscordProvider(): provider={id={}} was not enabled due to initial errors",
                    provider.getId());

            return errors;
        }

        Long suggestionChannelId = provider.getSuggestionChannelId();
        discordSuggestionService.addCategory(provider.getCategory().getId(), suggestionChannelId);

        errors = DiscordSuggestionValidator.validateFinally(provider, jda);
        if (!errors.isEmpty()) {
            discordSuggestionService.removeCategory(suggestionChannelId);

            logger.debug("enableDiscordProvider(): provider={id={}} was not enabled due to final errors",
                    provider.getId());

            return errors;
        }

        logger.debug("enableDiscordProvider(): provider={id={}} is enabled", provider.getId());

        return errors;
    }

    public void disableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        discordSuggestionService.removeCategory(provider.getSuggestionChannelId());

        logger.debug("disableDiscordProvider(): provider={id={}} is disabled", provider.getId());
    }

    @Nonnull
    public List<MessageEmbed> enableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        // todo
        List<MessageEmbed> errors = new ArrayList<>();

        logger.debug("enableVkProvider(): provider={id={}} is enabled", provider.getId());

        return errors;
    }

    public void disableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        // todo

        logger.debug("disableVkProvider(): provider={id={}} is disabled", provider.getId());
    }

    @Nonnull
    public List<MessageEmbed> enableApprove(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        List<MessageEmbed> errors = ApproveValidator.validateInitially(category);
        if (!errors.isEmpty()) {
            logger.debug("enableApprove(): Media approve for category={id={}} was not enabled due to initial errors",
                    category.getId());

            return errors;
        }

        approveService.addCategory(category);

        errors = ApproveValidator.validateFinally(category, jda);
        if (!errors.isEmpty()) {
            approveService.removeCategory(category);

            logger.debug("enableApprove(): Media approve for category={id={}} was not enabled due to final errors",
                    category.getId());

            return errors;
        }

        logger.debug("enableApprove(): Media approve for category={id={}} is enabled", category.getId());

        return errors;
    }

    public void disableApprove(@Nonnull DiscordCategory category) {
        approveService.removeCategory(category);

        logger.debug("disableApprove(): Media approve for category={id={}} is disabled", category.getId());
    }

    @Nonnull
    public List<MessageEmbed> enableSending(@Nonnull DiscordCategory category,
                                            @Nonnull MediaRequestSender mediaRequestSender,
                                            @Nonnull JDA jda) {

        List<MessageEmbed> errors = SendingValidator.validateInitially(category);
        if (!errors.isEmpty()) {
            logger.debug("enableSending(): Media sending for category={id={}} was not enabled due to initial errors",
                    category.getId());

            return errors;
        }

        sendingService.addCategory(category, mediaRequestSender);

        errors = SendingValidator.validateFinally(category, jda);
        if (!errors.isEmpty()) {
            sendingService.removeCategory(category.getId());

            logger.debug("enableSending(): Media sending for category={id={}} was not enabled due to final errors",
                    category.getId());

            return errors;
        }

        logger.debug("enableSending(): Media sending for category={id={}} is enabled", category.getId());

        return errors;
    }

    public void disableSending(@Nonnull DiscordCategory category) {
        sendingService.removeCategory(category.getId());

        logger.debug("disableSending(): Media sending for category={id={}} is disabled", category.getId());
    }
}
