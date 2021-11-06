package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@Component
public class ConfigWizardDiscordProvidersAddCommand implements ConfigWizardDiscordProvidersCommand {

    private static final String NAME = "add";

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersAddCommand.class);

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromDiscordService discordProviderService;

    @Autowired
    public ConfigWizardDiscordProvidersAddCommand(DiscordCategoryService categoryService,
                                                  DiscordProviderFromDiscordService discordProviderService) {
        this.categoryService = categoryService;
        this.discordProviderService = discordProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing");
            // todo internal error response
            return null;
        }

        if (argsString.isEmpty()) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name cannot be empty")
                    .build()
            );
            return null;
        }

        if (argsString.length() > 32) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build()
            );
            return null;
        }

        long categoryId = session.getEntityId();
        // todo use name index ?
        for (DiscordProviderFromDiscord p : discordProviderService.findAllByCategoryId(categoryId)) {
            if (p.getName().equals(argsString)) {
                session.getResponses().add(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("Discord provider name must be unique")
                        .build()
                );
                return null;
            }
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }

        DiscordProviderFromDiscord provider = new DiscordProviderFromDiscord();
        provider.setName(argsString);
        provider = discordProviderService.attach(provider, categoryOptional.get());

        session.setEntityId(provider.getId());

        logger.debug("Add at DISCORD_PROVIDERS: name={}, session={}", argsString, session);

        return ConfigWizardState.DISCORD_PROVIDER;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
