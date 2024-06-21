package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
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
 * The command for disabling a discord provider.
 */
@Component
public class ConfigWizardDiscordProviderDisableCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderDisableCommand.class);

    private static final String NAME = "disable";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final MediaActionService mediaActionService;

    /**
     * Constructs the command.
     *
     * @param discordProviderService an instance of {@link DiscordProviderFromDiscordService}
     * @param mediaActionService an instance of {@link MediaActionService}
     */
    @Autowired
    public ConfigWizardDiscordProviderDisableCommand(DiscordProviderFromDiscordService discordProviderService,
                                                     MediaActionService mediaActionService) {
        this.discordProviderService = discordProviderService;
        this.mediaActionService = mediaActionService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long providerId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromDiscord> providerOptional = discordProviderService.findById(providerId);
        if (providerOptional.isEmpty()) {
            logger.warn("execute(): Unable to get discordProvider={id={}} for privateSession={}", providerId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromDiscord provider = providerOptional.get();

        if (!provider.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider is already disabled")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        provider.setEnabled(false);
        provider = discordProviderService.save(provider);

        String message;
        if (provider.getCategory().isEnabled()) {
            mediaActionService.disableDiscordProvider(provider);
            message = "The provider has been disabled";
        } else {
            message = "The flag `enabled` has been unset";
        }
        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription(message)
                .build());

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
