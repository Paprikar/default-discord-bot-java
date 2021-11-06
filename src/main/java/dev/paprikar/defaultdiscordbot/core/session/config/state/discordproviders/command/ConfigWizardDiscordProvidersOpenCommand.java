package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

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
import java.util.List;

@Component
public class ConfigWizardDiscordProvidersOpenCommand implements ConfigWizardDiscordProvidersCommand {

    private static final String NAME = "open";

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersOpenCommand.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    @Autowired
    public ConfigWizardDiscordProvidersOpenCommand(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        List<DiscordProviderFromDiscord> providers = discordProviderService
                .findAllByCategoryId(session.getEntityId());
        // todo use name index ?
        DiscordProviderFromDiscord targetProvider = null;
        for (DiscordProviderFromDiscord p : providers) {
            if (p.getName().equals(argsString)) {
                targetProvider = p;
                break;
            }
        }
        if (targetProvider == null) {
            // todo illegal command response
            return null;
        }

        session.setEntityId(targetProvider.getId());

        logger.debug("Open at DISCORD_PROVIDERS: target='{}', session={}", argsString, session);

        return ConfigWizardState.DISCORD_PROVIDER;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
