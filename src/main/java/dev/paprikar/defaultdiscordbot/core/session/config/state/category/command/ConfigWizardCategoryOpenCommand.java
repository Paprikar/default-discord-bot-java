package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

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
public class ConfigWizardCategoryOpenCommand implements ConfigWizardCategoryCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryOpenCommand.class);

    private static final String NAME = "open";

    // Map<Directory, State>
    private final Map<String, ConfigWizardState> targets = new HashMap<>();

    @Autowired
    public ConfigWizardCategoryOpenCommand() {
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
        logger.debug("Open at CATEGORY: privateSession={}, target='{}'", session, argsString);
        return targetState;
    }

    private void setupTargets() {
        targets.put("discord providers", ConfigWizardState.DISCORD_PROVIDERS);
        targets.put("vk providers", ConfigWizardState.VK_PROVIDERS);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
