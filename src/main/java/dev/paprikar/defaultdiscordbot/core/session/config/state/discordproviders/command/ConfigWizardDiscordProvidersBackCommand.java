package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The command for switching from discord providers directory to category directory.
 */
@Component
public class ConfigWizardDiscordProvidersBackCommand implements ConfigWizardDiscordProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersBackCommand.class);

    private static final String NAME = "back";

    /**
     * Constructs the command.
     */
    @Autowired
    public ConfigWizardDiscordProvidersBackCommand() {
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        return ConfigWizardState.CATEGORY;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
