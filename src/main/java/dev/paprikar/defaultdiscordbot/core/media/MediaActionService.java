package dev.paprikar.defaultdiscordbot.core.media;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveValidator;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingValidator;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionValidator;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionValidator;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
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

/**
 * Service for managing media modules.
 */
@Service
public class MediaActionService {

    private static final Logger logger = LoggerFactory.getLogger(MediaActionService.class);

    private final DiscordProviderFromDiscordService discordProviderService;
    private final DiscordProviderFromVkService vkProviderService;
    private final DiscordSuggestionService discordSuggestionService;
    private final VkSuggestionService vkSuggestionService;
    private final ApproveService approveService;
    private final SendingService sendingService;
    private final SendingValidator sendingValidator;
    private final ApproveValidator approveValidator;
    private final DiscordSuggestionValidator discordSuggestionValidator;
    private final VkSuggestionValidator vkSuggestionValidator;

    /**
     * Constructs the service.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param discordSuggestionService
     *         an instance of {@link DiscordSuggestionService}
     * @param vkSuggestionService
     *         an instance of {@link VkSuggestionService}
     * @param approveService
     *         an instance of {@link ApproveService}
     * @param sendingService
     *         an instance of {@link SendingService}
     * @param sendingValidator
     *         an instance of {@link SendingValidator}
     * @param approveValidator
     *         an instance of {@link ApproveValidator}
     * @param discordSuggestionValidator
     *         an instance of {@link DiscordSuggestionValidator}
     * @param vkSuggestionValidator
     *         an instance of {@link VkSuggestionValidator}
     */
    @Autowired
    public MediaActionService(DiscordProviderFromDiscordService discordProviderService,
                              DiscordProviderFromVkService vkProviderService,
                              DiscordSuggestionService discordSuggestionService,
                              VkSuggestionService vkSuggestionService,
                              ApproveService approveService,
                              SendingService sendingService,
                              SendingValidator sendingValidator,
                              ApproveValidator approveValidator,
                              DiscordSuggestionValidator discordSuggestionValidator,
                              VkSuggestionValidator vkSuggestionValidator) {
        this.discordProviderService = discordProviderService;
        this.vkProviderService = vkProviderService;
        this.discordSuggestionService = discordSuggestionService;
        this.vkSuggestionService = vkSuggestionService;
        this.approveService = approveService;
        this.sendingService = sendingService;
        this.sendingValidator = sendingValidator;
        this.approveValidator = approveValidator;
        this.discordSuggestionValidator = discordSuggestionValidator;
        this.vkSuggestionValidator = vkSuggestionValidator;
    }

    // CATEGORY OPERATIONS

    /**
     * Enables the category with its children components.
     *
     * @param category
     *         the category
     *
     * @return the {@link List} of errors that occurred during category enabling
     */
    public List<MessageEmbed> enableCategory(@Nonnull DiscordCategory category) {
        List<MessageEmbed> errors = new ArrayList<>();

        JDA jda = JDAService.get();
        if (jda == null) {
            logger.error("enableCategory(): Failed to get jda");
            errors.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Category was not enabled due to internal errors")
                    .build());
            return errors;
        }

        errors.addAll(enableSending(category, jda));

        errors.addAll(enableApprove(category, jda));

        Long categoryId = category.getId();

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

    /**
     * Disables the category with its children components.
     *
     * @param category
     *         the category
     */
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

    /**
     * Enables the sending module of the category.
     *
     * @param category
     *         the category
     * @param jda
     *         an instance of {@link JDA}
     *
     * @return the {@link List} of errors that occurred during sending module enabling
     */
    public List<MessageEmbed> enableSending(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        Long categoryId = category.getId();

        List<MessageEmbed> errors = sendingValidator.validateInitially(category);
        if (!errors.isEmpty()) {
            logger.debug("enableSending(): Media sending for category={id={}} was not enabled due to initial errors",
                    categoryId);

            return errors;
        }

        sendingService.add(category, jda);

        errors = sendingValidator.validateFinally(category, jda);
        if (!errors.isEmpty()) {
            sendingService.remove(category);

            logger.debug("enableSending(): Media sending for category={id={}} was not enabled due to final errors",
                    categoryId);

            return errors;
        }

        logger.debug("enableSending(): Media sending for category={id={}} is enabled", categoryId);

        return errors;
    }

    /**
     * Disables the sending module of the category.
     *
     * @param category
     *         the category
     */
    public void disableSending(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        sendingService.remove(category);

        logger.debug("disableSending(): Media sending for category={id={}} is disabled", categoryId);
    }

    /**
     * Enables the approval module of the category.
     *
     * @param category
     *         the category
     *
     * @return the {@link List} of errors that occurred during approval module enabling
     */
    public List<MessageEmbed> enableApprove(@Nonnull DiscordCategory category) {
        JDA jda = JDAService.get();
        if (jda == null) {
            logger.error("enableApprove(): Failed to get jda");
            return List.of(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Approve module was not enabled due to internal errors")
                    .build());
        }

        return enableApprove(category, jda);
    }

    /**
     * Enables the approval module of the category.
     *
     * @param category
     *         the category
     * @param jda
     *         an instance of {@link JDA}
     *
     * @return the {@link List} of errors that occurred during approval module enabling
     */
    public List<MessageEmbed> enableApprove(@Nonnull DiscordCategory category, @Nonnull JDA jda) {
        Long categoryId = category.getId();

        List<MessageEmbed> errors = approveValidator.validateInitially(category);
        if (!errors.isEmpty()) {
            logger.debug("enableApprove(): Media approve for category={id={}} was not enabled due to initial errors",
                    categoryId);

            return errors;
        }

        approveService.add(category);

        errors = approveValidator.validateFinally(category, jda);
        if (!errors.isEmpty()) {
            approveService.remove(category);

            logger.debug("enableApprove(): Media approve for category={id={}} was not enabled due to final errors",
                    categoryId);

            return errors;
        }

        logger.debug("enableApprove(): Media approve for category={id={}} is enabled", categoryId);

        return errors;
    }

    /**
     * Disables the approval module of the category.
     *
     * @param category
     *         the category
     */
    public void disableApprove(@Nonnull DiscordCategory category) {
        approveService.remove(category);

        logger.debug("disableApprove(): Media approve for category={id={}} is disabled", category.getId());
    }

    // DISCORD PROVIDER OPERATIONS

    /**
     * Enables the discord provider of the category.
     *
     * @param provider
     *         the discord provider
     *
     * @return the {@link List} of errors that occurred during discord provider enabling
     */
    public List<MessageEmbed> enableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        JDA jda = JDAService.get();
        if (jda == null) {
            logger.error("enableDiscordProvider(): Failed to get jda");
            return List.of(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Provider was not enabled due to internal errors")
                    .build());
        }

        return enableDiscordProvider(provider, jda);
    }

    /**
     * Enables the discord provider of the category.
     *
     * @param provider
     *         the discord provider
     * @param jda
     *         an instance of {@link JDA}
     *
     * @return the {@link List} of errors that occurred during discord provider enabling
     */
    public List<MessageEmbed> enableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider, @Nonnull JDA jda) {
        Long providerId = provider.getId();

        List<MessageEmbed> errors = discordSuggestionValidator.validateInitially(provider);
        if (!errors.isEmpty()) {
            logger.debug("enableDiscordProvider(): provider={id={}} was not enabled due to initial errors",
                    providerId);

            return errors;
        }

        discordSuggestionService.add(provider);

        errors = discordSuggestionValidator.validateFinally(provider, jda);
        if (!errors.isEmpty()) {
            discordSuggestionService.remove(provider);

            logger.debug("enableDiscordProvider(): provider={id={}} was not enabled due to final errors",
                    providerId);

            return errors;
        }

        logger.debug("enableDiscordProvider(): provider={id={}} is enabled", providerId);

        return errors;
    }

    /**
     * Disables the discord provider of the category.
     *
     * @param provider
     *         the discord provider
     */
    public void disableDiscordProvider(@Nonnull DiscordProviderFromDiscord provider) {
        discordSuggestionService.remove(provider);

        logger.debug("disableDiscordProvider(): provider={id={}} is disabled", provider.getId());
    }

    // VK PROVIDER OPERATIONS

    /**
     * Enables the vk provider of the category.
     *
     * @param provider
     *         the discord provider
     *
     * @return the {@link List} of errors that occurred during vk provider enabling
     */
    public List<MessageEmbed> enableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        Long providerId = provider.getId();

        List<MessageEmbed> errors = vkSuggestionValidator.validateInitially(provider);
        if (!errors.isEmpty()) {
            logger.debug("enableVkProvider(): provider={id={}} was not enabled due to initial errors",
                    providerId);

            return errors;
        }

        errors = vkSuggestionValidator.validateFinally(provider);
        if (!errors.isEmpty()) {
            logger.debug("enableVkProvider(): provider={id={}} was not enabled due to final errors",
                    providerId);

            return errors;
        }

        vkSuggestionService.add(provider);

        logger.debug("enableVkProvider(): provider={id={}} is enabled", providerId);

        return errors;
    }

    /**
     * Disables the vk provider of the category.
     *
     * @param provider
     *         the discord provider
     */
    public void disableVkProvider(@Nonnull DiscordProviderFromVk provider) {
        vkSuggestionService.remove(provider);

        logger.debug("disableVkProvider(): provider={id={}} is disabled", provider.getId());
    }
}
