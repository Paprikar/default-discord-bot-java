package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

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
public class ConfigWizardDiscordProviderBackCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderBackCommand.class);

    private static final String NAME = "back";

    private final DiscordProviderFromDiscordService discordProviderService;

    @Autowired
    public ConfigWizardDiscordProviderBackCommand(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService.findById(entityId);
        if (discordProviderOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}}, "
                    + "ending privateSession={}", entityId, session);

            return ConfigWizardState.END;
        }

        session.setEntityId(discordProviderOptional.get().getCategory().getId());
        return ConfigWizardState.DISCORD_PROVIDERS;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
