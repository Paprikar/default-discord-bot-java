package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggester;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.validation.ConfigWizardTrustedSuggesterIdValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
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
 * The command to remove trusted suggesters.
 */
@Component
public class ConfigWizardTrustedSuggestersRemoveCommand implements ConfigWizardTrustedSuggestersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardTrustedSuggestersRemoveCommand.class);

    private static final String NAME = "remove";

    private final DiscordTrustedSuggesterService trustedSuggesterService;
    private final ConfigWizardTrustedSuggesterIdValidator validator;

    /**
     * Constructs the command.
     *
     * @param trustedSuggesterService
     *         an instance of {@link DiscordTrustedSuggesterService}
     * @param validator
     *         an instance of {@link ConfigWizardTrustedSuggesterIdValidator}
     */
    @Autowired
    public ConfigWizardTrustedSuggestersRemoveCommand(DiscordTrustedSuggesterService trustedSuggesterService,
                                                      ConfigWizardTrustedSuggesterIdValidator validator) {
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

        Optional<DiscordTrustedSuggester> suggesterOptional = trustedSuggesterService
                .findByCategoryIdAndUserId(categoryId, userId);
        if (suggesterOptional.isEmpty()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The suggester with id `" + argsString + "` does not exist")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }
        DiscordTrustedSuggester suggester = suggesterOptional.get();

        trustedSuggesterService.delete(suggester);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The suggester " + User.fromId(argsString).getAsMention() + " has been removed")
                .build()
        );

        logger.debug("The suggester={id={}} was deleted", suggester.getId());
        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
