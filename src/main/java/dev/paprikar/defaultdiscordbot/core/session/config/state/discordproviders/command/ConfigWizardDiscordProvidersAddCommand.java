package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardDiscordProvidersAddCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersAddCommand.class);

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromDiscordService discordProviderService;

    public ConfigWizardDiscordProvidersAddCommand(
            DiscordCategoryService categoryService,
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
        for (DiscordProviderFromDiscord p : discordProviderService.findProvidersByCategoryId(categoryId)) {
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
        DiscordProviderFromDiscord provider = new DiscordProviderFromDiscord();
        provider.setName(argsString);
        DiscordCategory category = categoryService.getCategoryById(categoryId);
        provider = discordProviderService.attach(provider, category);

        session.setEntityId(provider.getId());

        logger.debug("Add at DISCORD_PROVIDERS: name={}, session={}", argsString, session);
        return ConfigWizardState.DISCORD_PROVIDER;
    }
}
