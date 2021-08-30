package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigWizardDiscordProviderDisableCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderDisableCommand.class);

    public ConfigWizardDiscordProviderDisableCommand() {
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("onExitCommand(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        // todo
        return null;
    }
}
