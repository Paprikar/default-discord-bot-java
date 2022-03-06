package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggester;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.validation.ConfigWizardTrustedSuggestersMentionValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The command to add trusted suggesters.
 */
@Component
public class ConfigWizardTrustedSuggestersAddCommand implements ConfigWizardTrustedSuggestersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardTrustedSuggestersAddCommand.class);

    private static final String NAME = "add";

    private final DiscordCategoryService categoryService;
    private final DiscordTrustedSuggesterService trustedSuggesterService;
    private final ConfigWizardTrustedSuggestersMentionValidator validator;

    /**
     * Constructs the command.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param trustedSuggesterService
     *         an instance of {@link DiscordTrustedSuggesterService}
     * @param validator
     *         an instance of {@link ConfigWizardTrustedSuggestersMentionValidator}
     */
    @Autowired
    public ConfigWizardTrustedSuggestersAddCommand(DiscordCategoryService categoryService,
                                                   DiscordTrustedSuggesterService trustedSuggesterService,
                                                   ConfigWizardTrustedSuggestersMentionValidator validator) {
        this.categoryService = categoryService;
        this.trustedSuggesterService = trustedSuggesterService;
        this.validator = validator;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long categoryId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing for privateSession={}", session);
            return ConfigWizardState.IGNORE;
        }

        DiscordValidatorProcessingResponse<Long> response = validator.process(argsString);
        Long userId = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }
        assert userId != null;

        error = validator.test(userId, categoryId);
        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            logger.warn("execute(): Unable to get category={id={}} for privateSession={}", categoryId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordCategory category = categoryOptional.get();

        DiscordTrustedSuggester suggester = new DiscordTrustedSuggester();

        suggester.setCategory(category);
        suggester.setUserId(userId);

        suggester = trustedSuggesterService.save(suggester);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The suggester " + argsString + " has been added")
                .build()
        );

        logger.debug("Add at TRUSTED_SUGGESTERS: privateSession={}, suggester={}", session, suggester);
        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
