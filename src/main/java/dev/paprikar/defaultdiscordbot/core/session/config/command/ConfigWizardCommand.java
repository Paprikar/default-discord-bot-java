package dev.paprikar.defaultdiscordbot.core.session.config.command;

import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConfigWizardCommand {

    @Nullable
    ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                              @Nonnull PrivateSession session,
                              @Nullable String argsString);
}
