package dev.paprikar.defaultdiscordbot.core.session.config.command;

import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardExitCommand implements ConfigWizardCommand {

    private static final String NAME = "exit";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardExitCommand.class);

    @Override
    @Nullable
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        session.getResponses().add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Configuration session is ended")
                .build()
        );
        return ConfigWizardState.END;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
