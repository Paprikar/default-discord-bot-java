package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.ConfigWizardDiscordProviderService;
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
public class ConfigWizardDiscordProviderDisableCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderDisableCommand.class);

    private static final String NAME = "disable";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final MediaActionService mediaActionService;

    @Autowired
    public ConfigWizardDiscordProviderDisableCommand(DiscordProviderFromDiscordService discordProviderService,
                                                     MediaActionService mediaActionService) {
        this.discordProviderService = discordProviderService;
        this.mediaActionService = mediaActionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromDiscord> providerOptional = discordProviderService.findById(entityId);
        if (providerOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordProviderFromDiscord provider = providerOptional.get();

        if (!provider.isEnabled()) {
            // todo already disabled response

            responses.add(ConfigWizardDiscordProviderService.getStateEmbed(provider));

            return null;
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

        responses.add(ConfigWizardDiscordProviderService.getStateEmbed(provider));

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
