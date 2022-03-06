package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * The command for switching from trusted suggesters directory to category directory.
 */
@Component
public class ConfigWizardTrustedSuggestersBackCommand implements ConfigWizardTrustedSuggestersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardTrustedSuggestersBackCommand.class);

    private static final String NAME = "back";

    /**
     * Constructs the command.
     */
    @Autowired
    public ConfigWizardTrustedSuggestersBackCommand() {
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
