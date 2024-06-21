package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.config.DdbDefaults;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing discord bot.
 */
@Service
public class DiscordBotService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBotService.class);

    private final DiscordGuildService guildService;
    private final DiscordCategoryService categoryService;
    private final DiscordMediaRequestService mediaRequestService;
    private final DiscordProviderFromDiscordService discordProviderService;
    private final DiscordProviderFromVkService vkProviderService;
    private final DiscordTrustedSuggesterService trustedSuggesterService;
    private final MediaActionService mediaActionService;
    private final DdbConfig config;


    /**
     * @param guildService an instance of {@link DiscordGuildService}
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param mediaRequestService an instance of {@link DiscordMediaRequestService}
     * @param discordProviderService an instance of {@link DiscordProviderFromDiscordService}
     * @param vkProviderService an instance of {@link DiscordProviderFromVkService}
     * @param trustedSuggesterService an instance of {@link DiscordTrustedSuggesterService}
     * @param mediaActionService an instance of {@link MediaActionService}
     * @param config an instance of {@link DdbConfig}
     */
    @Autowired
    public DiscordBotService(DiscordGuildService guildService,
                             DiscordCategoryService categoryService,
                             DiscordMediaRequestService mediaRequestService,
                             DiscordProviderFromDiscordService discordProviderService,
                             DiscordProviderFromVkService vkProviderService,
                             DiscordTrustedSuggesterService trustedSuggesterService,
                             MediaActionService mediaActionService,
                             DdbConfig config) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.discordProviderService = discordProviderService;
        this.vkProviderService = vkProviderService;
        this.trustedSuggesterService = trustedSuggesterService;
        this.mediaActionService = mediaActionService;
        this.config = config;
    }

    /**
     * Sets the bot modules to working state.
     *
     * @param jda an instance of {@link JDA}
     */
    @Transactional
    public void initialize(@Nonnull JDA jda) {
        logger.debug("initialize(): Initialization is started");

        Set<Long> connectedGuildIds = jda.getGuilds().stream()
                .map(ISnowflake::getIdLong)
                .collect(Collectors.toSet());

        Set<Long> savedGuildIds = guildService.findAll().stream()
                .map(DiscordGuild::getDiscordId)
                .collect(Collectors.toSet());

        Set<Long> toSetupGuildIds = connectedGuildIds.stream()
                .filter(id -> !savedGuildIds.contains(id))
                .collect(Collectors.toSet());

        Set<Long> toDeleteGuildIds = savedGuildIds.stream()
                .filter(id -> !connectedGuildIds.contains(id))
                .collect(Collectors.toSet());

        for (Long id : toSetupGuildIds) {
            logger.debug("initialize(): Setting up the guild={discordId={}} after a period of inactivity", id);
            setupDiscordGuild(id);
        }

        for (Long id : toDeleteGuildIds) {
            logger.debug("initialize(): Deleting the guild={discordId={}} after a period of inactivity", id);
            deleteDiscordGuild(id);
        }

        categoryService.findAll().stream()
                .filter(DiscordCategory::isEnabled)
                .forEach(mediaActionService::enableCategory);

        logger.debug("initialize(): Initialization is finished");
    }

    /**
     * Stops discord bot modules.
     */
    public void shutdown() {
        logger.debug("shutdown(): Shutdown is started");

        categoryService.findAll().forEach(category -> {
            Long categoryId = category.getId();

            discordProviderService
                    .findAllByCategoryId(categoryId)
                    .stream()
                    .filter(DiscordProviderFromDiscord::isEnabled)
                    .forEach(mediaActionService::disableDiscordProvider);

            mediaActionService.disableApprove(category);

            mediaActionService.disableSending(category);
        });

        logger.debug("shutdown(): Shutdown is finished");
    }

    /**
     * Prepares the bot to work with the new guild.
     *
     * @param guildDiscordId the guild discord id
     */
    @Transactional
    public void setupDiscordGuild(long guildDiscordId) {
        DiscordGuild guild = new DiscordGuild();
        DdbDefaults defaults = config.getDefaults();

        guild.setDiscordId(guildDiscordId);
        guild.setPrefix(defaults.getPrefix());

        guildService.save(guild);

        logger.debug("setupDiscordGuild(): The guild={discordId={}} is set up", guildDiscordId);
    }

    /**
     * Deletes guild data.
     *
     * @param guildDiscordId the guild discord id
     */
    @Transactional
    public void deleteDiscordGuild(long guildDiscordId) {
        // todo delete timeout

        categoryService.findAllByGuildDiscordId(guildDiscordId).forEach(category -> {
            mediaActionService.disableCategory(category);
            Long categoryId = category.getId();

            categoryService.deleteById(categoryId);
            mediaRequestService.deleteByCategoryId(categoryId);
            discordProviderService.deleteByCategoryId(categoryId);
            vkProviderService.deleteByCategoryId(categoryId);
            trustedSuggesterService.deleteByCategoryId(categoryId);
        });

        guildService.deleteByDiscordId(guildDiscordId);

        logger.debug("deleteDiscordGuild(): The guild={discordId={}} is deleted", guildDiscordId);
    }
}
