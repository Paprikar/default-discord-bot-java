package dev.paprikar.defaultdiscordbot.core.session.config.state.root.command;

import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigWizardRootOpenCommand implements ConfigWizardRootCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootOpenCommand.class);

    private static final String NAME = "open";

    // Map<Directory, State>
    private final Map<String, ConfigWizardState> targets = new HashMap<>();

    @Autowired
    public ConfigWizardRootOpenCommand() {
        setupTargets();
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing");
            // todo internal error response
            return null;
        }

        ConfigWizardState targetState = targets.get(argsString);
        if (targetState == null) {
            // todo illegal command response
            return null;
        }

        logger.debug("Open at ROOT: targetState='{}'", argsString);

        return targetState;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void setupTargets() {
        targets.put("categories", ConfigWizardState.CATEGORIES);
    }
}
