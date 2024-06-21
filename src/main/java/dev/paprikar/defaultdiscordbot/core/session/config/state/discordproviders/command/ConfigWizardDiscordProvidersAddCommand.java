package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.validation.ConfigWizardDiscordProviderNameValidator;
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
 * The command to add discord providers.
 */
@Component
public class ConfigWizardDiscordProvidersAddCommand implements ConfigWizardDiscordProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersAddCommand.class);

    private static final String NAME = "add";

    private final DiscordCategoryService categoryService;
    private final DiscordProviderFromDiscordService discordProviderService;
    private final ConfigWizardDiscordProviderNameValidator validator;

    /**
     * Constructs the command.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param discordProviderService an instance of {@link DiscordProviderFromDiscordService}
     * @param validator an instance of {@link ConfigWizardDiscordProviderNameValidator}
     */
    @Autowired
    public ConfigWizardDiscordProvidersAddCommand(DiscordCategoryService categoryService,
                                                  DiscordProviderFromDiscordService discordProviderService,
                                                  ConfigWizardDiscordProviderNameValidator validator) {
        this.categoryService = categoryService;
        this.discordProviderService = discordProviderService;
        this.validator = validator;
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

        Long categoryId = session.getEntityId();
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            logger.warn("execute(): Unable to get category={id={}} for privateSession={}", categoryId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordCategory category = categoryOptional.get();

        DiscordProviderFromDiscord provider = new DiscordProviderFromDiscord();

        provider.setCategory(category);

        DiscordValidatorProcessingResponse<String> response = validator.process(argsString, provider);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }
        assert name != null;

        error = validator.test(name, categoryId);
        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }

        provider.setName(name);
        discordProviderService.save(provider);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The provider `" + argsString + "` has been added")
                .build()
        );

        logger.debug("Add at DISCORD_PROVIDERS: privateSession={}, name='{}'", session, name);
        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
