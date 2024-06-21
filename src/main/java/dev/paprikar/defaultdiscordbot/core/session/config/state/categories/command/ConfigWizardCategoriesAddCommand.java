package dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.config.DdbDefaults;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation.ConfigWizardCategoryNameValidator;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The command to add categories.
 */
@Component
public class ConfigWizardCategoriesAddCommand implements ConfigWizardCategoriesCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesAddCommand.class);

    private static final String NAME = "add";

    private final DiscordGuildService guildService;
    private final DiscordCategoryService categoryService;
    private final ConfigWizardCategoryNameValidator validator;
    private final DdbConfig config;

    /**
     * Constructs the command.
     *
     * @param guildService an instance of {@link DiscordGuildService}
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param validator an instance of {@link ConfigWizardCategoryNameValidator}
     * @param config an instance of {@link DdbConfig}
     */
    @Autowired
    public ConfigWizardCategoriesAddCommand(DiscordGuildService guildService,
                                            DiscordCategoryService categoryService,
                                            ConfigWizardCategoryNameValidator validator,
                                            DdbConfig config) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.validator = validator;
        this.config = config;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        List<MessageEmbed> responses = session.getResponses();

        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing for privateSession={}", session);
            return ConfigWizardState.IGNORE;
        }

        Long guildId = session.getEntityId();
        Optional<DiscordGuild> guildOptional = guildService.findById(guildId);
        if (guildOptional.isEmpty()) {
            logger.warn("execute(): Unable to get guild={id={}} for privateSession={}", guildId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordGuild guild = guildOptional.get();

        DiscordCategory category = new DiscordCategory();
        DdbDefaults defaults = config.getDefaults();

        category.setGuild(guild);
        category.setPositiveApprovalEmoji(defaults.getPositiveApprovalEmoji());
        category.setNegativeApprovalEmoji(defaults.getNegativeApprovalEmoji());

        DiscordValidatorProcessingResponse<String> response = validator.process(argsString, category);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }
        assert name != null;

        error = validator.test(name, guildId);
        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }

        category.setName(name);
        categoryService.save(category);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The category `" + argsString + "` has been added")
                .build()
        );

        logger.debug("Add at CATEGORIES: privateSession={}, name='{}'", session, name);
        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
