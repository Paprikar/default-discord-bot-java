package dev.paprikar.defaultdiscordbot.core.session.config.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

/**
 * The command for ending a configuration session.
 */
public class ConfigWizardExitCommand implements ConfigWizardCommand {

    private static final String NAME = "exit";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardExitCommand.class);

    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        session.getResponses().add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Configuration session is ended")
                .build()
        );

        return ConfigWizardState.END;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
