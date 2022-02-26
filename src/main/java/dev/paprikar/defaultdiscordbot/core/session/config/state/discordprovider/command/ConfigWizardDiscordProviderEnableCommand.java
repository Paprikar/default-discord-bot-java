package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
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
 * The command for enabling a discord provider.
 */
@Component
public class ConfigWizardDiscordProviderEnableCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderEnableCommand.class);

    private static final String NAME = "enable";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final MediaActionService mediaActionService;

    /**
     * Constructs the command.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     * @param mediaActionService
     *         an instance of {@link MediaActionService}
     */
    @Autowired
    public ConfigWizardDiscordProviderEnableCommand(
            DiscordProviderFromDiscordService discordProviderService,
            MediaActionService mediaActionService) {
        this.discordProviderService = discordProviderService;
        this.mediaActionService = mediaActionService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromDiscord> providerOptional = discordProviderService.findById(entityId);
        if (providerOptional.isEmpty()) {
            logger.warn("execute(): Unable to get discordProvider={id={}} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromDiscord provider = providerOptional.get();

        if (provider.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider is already enabled")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        provider.setEnabled(true);
        provider = discordProviderService.save(provider);

        if (provider.getCategory().isEnabled()) {
            List<MessageEmbed> errors = mediaActionService.enableDiscordProvider(provider);
            responses.addAll(errors);

            if (errors.isEmpty()) {
                responses.add(new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTitle("Configuration Wizard")
                        .setTimestamp(Instant.now())
                        .appendDescription("The provider has been enabled")
                        .build());
            }
        } else {
            responses.add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("The flag `enabled` has been set")
                    .build());
        }

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
