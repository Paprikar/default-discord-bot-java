package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Component
public class ConfigWizardDiscordProviderEnableCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderEnableCommand.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    private final MediaActionService mediaActionService;

    private final ReadWriteLockService readWriteLockService;

    @Autowired
    public ConfigWizardDiscordProviderEnableCommand(DiscordProviderFromDiscordService discordProviderService,
                                                    MediaActionService mediaActionService,
                                                    ReadWriteLockService readWriteLockService) {
        this.discordProviderService = discordProviderService;
        this.mediaActionService = mediaActionService;
        this.readWriteLockService = readWriteLockService;
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

        if (provider.isEnabled()) {
            // todo already enabled response
            return null;
        }

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, provider.getCategory().getGuild().getId());
        if (lock == null) {
            // todo error response
            return null;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        provider.setEnabled(true);
        discordProviderService.save(provider);

        mediaActionService.enableDiscordProvider(provider);

        writeLock.unlock();

        // todo enabled response

        return null;
    }
}
