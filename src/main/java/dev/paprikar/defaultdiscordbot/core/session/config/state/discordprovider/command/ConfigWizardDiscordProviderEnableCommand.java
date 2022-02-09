package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.ConfigWizardDiscordProviderDescriptionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ConfigWizardDiscordProviderEnableCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderEnableCommand.class);

    private static final String NAME = "enable";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final MediaActionService mediaActionService;
    private final ConfigWizardDiscordProviderDescriptionService descriptionService;

    @Autowired
    public ConfigWizardDiscordProviderEnableCommand(
            DiscordProviderFromDiscordService discordProviderService,
            MediaActionService mediaActionService,
            ConfigWizardDiscordProviderDescriptionService descriptionService) {
        this.discordProviderService = discordProviderService;
        this.mediaActionService = mediaActionService;
        this.descriptionService = descriptionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromDiscord> providerOptional = discordProviderService.findById(entityId);
        if (providerOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}}, "
                    + "ending privateSession={}", entityId, session);

            return ConfigWizardState.END;
        }
        DiscordProviderFromDiscord provider = providerOptional.get();

        if (provider.isEnabled()) {
            // todo already enabled response

            responses.add(descriptionService.getDescription(provider));

            return null;
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

        responses.add(descriptionService.getDescription(provider));

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
