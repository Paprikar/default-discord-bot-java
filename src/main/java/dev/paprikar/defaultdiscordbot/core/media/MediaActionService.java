package dev.paprikar.defaultdiscordbot.core.media;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveValidator;
import dev.paprikar.defaultdiscordbot.core.media.sending.MediaRequestSender;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingValidator;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionValidator;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionValidator;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediaActionService {

    private static final Logger logger = LoggerFactory.getLogger(MediaActionService.class);

    private final DiscordCategoryService categoryService;

    private final DiscordMediaRequestService mediaRequestService;

    private final DiscordProviderFromDiscordService discordProviderService;

    private final DiscordProviderFromVkService vkProviderService;

    private final DiscordSuggestionService discordSuggestionService;

    private final VkSuggestionService vkSuggestionService;

    private final ApproveService approveService;

    private final SendingService sendingService;

    private final JDAService jdaService;

    @Autowired
    public MediaActionService(DiscordCategoryService categoryService,
                              DiscordMediaRequestService mediaRequestService,
                              DiscordProviderFromDiscordService discordProviderService,
                              DiscordProviderFromVkService vkProviderService,
                              DiscordSuggestionService discordSuggestionService,
                              VkSuggestionService vkSuggestionService,
                              ApproveService approveService,
                              SendingService sendingService,
                              JDAService jdaService) {
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.discordProviderService = discordProviderService;
        this.vkProviderService = vkProviderService;
        this.discordSuggestionService = discordSuggestionService;
        this.vkSuggestionService = vkSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.jdaService = jdaService;
    }

    // CATEGORY OPERATIONS

    @Nonnull
    public List<MessageEmbed> enableCategory(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        List<MessageEmbed> errors = new ArrayList<>();

        JDA jda = jdaService.get();
        if (jda == null) {
            logger.warn("enableCategory(): Failed to get jda");
            errors.add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Category was not enabled due to internal errors")
                    .build());
            return errors;
        }

        errors.addAll(enableSending(category, new MediaRequestSender(jda, mediaRequestService, categoryService), jda));

        errors.addAll(enableApprove(category, jda));

        discordProviderService
                .findAllByCategoryId(categoryId)
                .stream()
                .filter(DiscordProviderFromDiscord::isEnabled)
                .forEach(provider -> errors.addAll(enableDiscordProvider(provider, jda)));

        vkProviderService
                .findAllByCategoryId(categoryId)
                .stream()
                .filter(DiscordProviderFromVk::isEnabled)
                .forEach(provider -> errors.addAll(enableVkProvider(provider)));

        logger.debug("enableCategory(): category={id={}} is enabled", categoryId);

        return errors;
    }

    public void disableCategory(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        discordProviderService
                .findAllByCategoryId(categoryId)
                .stream()
                .filter(DiscordProviderFromDiscord::isEnabled)
                .forEach(this::disableDiscordProvider);

        vkProviderService
                .findAllByCategoryId(categoryId)
                .stream()
                .filter(DiscordProviderFromVk::isEnabled)
                .forEach(this::disableVkProvider);

        disableApprove(category);

        disableSending(category);

        logger.debug("disableCategory(): category={id={}} is disabled", categoryId);
    }

    @Nonnull
    private List<MessageEmbed> enableSending(@Nonnull DiscordCategory category,
                                             @Nonnull MediaRequestSender mediaRequestSender,
                                             @Nonnull JDA jda) {
        Long categoryId = category.getId();

        List<MessageEmbed> errors = SendingValidator.validateInitially(category);
        if (!errors.isEmpty()) {
            logger.debug("enableSending(): Media sending for category={id={}} was not enabled due to initial errors",
                    categoryId);

            return errors;
        }

        sendingService.add(category, mediaRequestSender);

        errors = SendingValidator.validateFinally(category, jda);
        if (!errors.isEmpty()) {
            sendingService.remove(categoryId);

            logger.debug("enableSending(): Media sending for category={id={}} was not enabled due to final errors",
                    categoryId);

            return errors;
        }

        logger.debug("enableSending(): Media sending for category={id={}} is enabled", categoryId);

        return errors;
    }

    private void disableSending(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        sendingService.remove(categoryId);

        logger.debug("disableSending(): Media sending for category={id={}} is disabled", categoryId);
    }

    @Nonnull
    private List<MessageEmbed> enableApprove(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        Long categoryId = category.getId();

        List<MessageEmbed> errors = ApproveValidator.validateInitially(category);
        if (!errors.isEmpty()) {
            logger.debug("enableApprove(): Media approve for category={id={}} was not enabled due to initial errors",
                    categoryId);

            return errors;
        }

        approveService.add(category);

        errors = ApproveValidator.validateFinally(category, jda);
        if (!errors.isEmpty()) {
            approveService.remove(category);

            logger.debug("enableApprove(): Media approve for category={id={}} was not enabled due to final errors",
                    categoryId);

            return errors;
        }

        logger.debug("enableApprove(): Media approve for category={id={}} is enabled", categoryId);

        return errors;
    }

    private void disableApprove(@Nonnull DiscordCategory category) {
        approveService.remove(category);

        logger.debug("disableApprove(): Media approve for category={id={}} is disabled", category.getId());
    }

    // DISCORD PROVIDER OPERATIONS

    @Nonnull
    public List<MessageEmbed> enableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        JDA jda = jdaService.get();
        if (jda == null) {
            logger.warn("enableDiscordProvider(): Failed to get jda");
            return List.of(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Provider was not enabled due to internal errors")
                    .build());
        }

        return enableDiscordProvider(provider, jda);
    }

    @Nonnull
    private List<MessageEmbed> enableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider, @Nonnull JDA jda) {
        Long providerId = provider.getId();

        List<MessageEmbed> errors = DiscordSuggestionValidator.validateInitially(provider);
        if (!errors.isEmpty()) {
            logger.debug("enableDiscordProvider(): provider={id={}} was not enabled due to initial errors",
                    providerId);

            return errors;
        }

        Long suggestionChannelId = provider.getSuggestionChannelId();
        discordSuggestionService.add(provider.getCategory().getId(), suggestionChannelId);

        errors = DiscordSuggestionValidator.validateFinally(provider, jda);
        if (!errors.isEmpty()) {
            discordSuggestionService.remove(suggestionChannelId);

            logger.debug("enableDiscordProvider(): provider={id={}} was not enabled due to final errors",
                    providerId);

            return errors;
        }

        logger.debug("enableDiscordProvider(): provider={id={}} is enabled", providerId);

        return errors;
    }

    public void disableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        discordSuggestionService.remove(provider.getSuggestionChannelId());

        logger.debug("disableDiscordProvider(): provider={id={}} is disabled", provider.getId());
    }

    // VK PROVIDER OPERATIONS

    @Nonnull
    public List<MessageEmbed> enableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();

        List<MessageEmbed> errors = VkSuggestionValidator.validateInitially(provider);
        if (!errors.isEmpty()) {
            logger.debug("enableVkProvider(): provider={id={}} was not enabled due to initial errors",
                    providerId);

            return errors;
        }

        errors = VkSuggestionValidator.validateFinally(provider, vkSuggestionService.getClient());
        if (!errors.isEmpty()) {
            vkSuggestionService.remove(provider);

            logger.debug("enableVkProvider(): provider={id={}} was not enabled due to final errors",
                    providerId);

            return errors;
        }

        vkSuggestionService.add(provider);

        logger.debug("enableVkProvider(): provider={id={}} is enabled", providerId);

        return errors;
    }

    public void disableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        vkSuggestionService.remove(provider);

        logger.debug("disableVkProvider(): provider={id={}} is disabled", provider.getId());
    }
}
