package dev.paprikar.defaultdiscordbot.core.session.config.command;

import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import javax.annotation.Nonnull;

public interface ConfigWizardCommand {

    ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                              @Nonnull PrivateSession session,
                              String argsString);

    String getName();
}
