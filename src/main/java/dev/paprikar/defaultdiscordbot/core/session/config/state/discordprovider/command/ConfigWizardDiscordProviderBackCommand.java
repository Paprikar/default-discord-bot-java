package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * The command for switching from discord provider directory to discord providers directory.
 */
@Component
public class ConfigWizardDiscordProviderBackCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderBackCommand.class);

    private static final String NAME = "back";

    private final DiscordProviderFromDiscordService discordProviderService;

    /**
     * Constructs the command.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     */
    @Autowired
    public ConfigWizardDiscordProviderBackCommand(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long providerId = session.getEntityId();

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService.findById(providerId);
        if (discordProviderOptional.isEmpty()) {
            logger.warn("execute(): Unable to get discordProvider={id={}} for privateSession={}", providerId, session);
            return ConfigWizardState.IGNORE;
        }

        session.setEntityId(discordProviderOptional.get().getCategory().getId());
        return ConfigWizardState.DISCORD_PROVIDERS;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
