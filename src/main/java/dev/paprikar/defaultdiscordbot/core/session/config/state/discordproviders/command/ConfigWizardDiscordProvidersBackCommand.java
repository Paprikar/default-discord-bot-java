package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command;

import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public class ConfigWizardDiscordProvidersBackCommand implements ConfigWizardDiscordProvidersCommand {

    private static final String NAME = "back";

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersBackCommand.class);

    @Autowired
    public ConfigWizardDiscordProvidersBackCommand() {
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        return ConfigWizardState.CATEGORY;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
