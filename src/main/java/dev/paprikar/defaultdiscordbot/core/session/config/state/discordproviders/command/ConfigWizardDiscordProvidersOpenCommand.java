package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

/**
 * The command for switching to a discord provider.
 */
@Component
public class ConfigWizardDiscordProvidersOpenCommand implements ConfigWizardDiscordProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersOpenCommand.class);

    private static final String NAME = "open";

    private final DiscordProviderFromDiscordService discordProviderService;

    /**
     * Constructs the command.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     */
    @Autowired
    public ConfigWizardDiscordProvidersOpenCommand(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        List<DiscordProviderFromDiscord> providers = discordProviderService.findAllByCategoryId(session.getEntityId());
        DiscordProviderFromDiscord targetProvider = providers.stream()
                .filter(provider -> argsString.equals(provider.getName()))
                .findFirst()
                .orElse(null);
        if (targetProvider == null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider with the name `" + argsString + "` does not exist")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        session.setEntityId(targetProvider.getId());

        logger.debug("Open at DISCORD_PROVIDERS: privateSession={}, target='{}'", session, argsString);

        return ConfigWizardState.DISCORD_PROVIDER;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
