package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Component
public class ConfigWizardDiscordProviderDisableCommand implements ConfigWizardDiscordProviderCommand {

    private static final String NAME = "disable";

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderDisableCommand.class);

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
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService
                .findById(session.getEntityId());
        if (!discordProviderOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordProviderFromDiscord provider = discordProviderOptional.get();

        if (!provider.isEnabled()) {
            // todo already disabled response
            return null;
        }

        provider.setEnabled(false);
        provider = discordProviderService.save(provider);

        mediaActionService.disableDiscordProvider(provider);

        // todo disabled response

        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
