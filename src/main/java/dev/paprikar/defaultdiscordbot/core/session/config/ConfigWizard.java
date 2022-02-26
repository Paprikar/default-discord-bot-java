package dev.paprikar.defaultdiscordbot.core.session.config;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import javax.annotation.Nonnull;

/**
 * An interface for describing configuration state services.
 */
public interface ConfigWizard {

    /**
     * Handles private messages within a configuration session.
     *
     * @param event
     *         the event of type {@link PrivateMessageReceivedEvent} for handling
     * @param session
     *         the configuration session
     *
     * @return the state of type {@link ConfigWizardState} after handling
     */
    ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull ConfigWizardSession session);

    /**
     * Sends a response as part of a configuration session.
     *
     * @param session
     *         the configuration session
     * @param addStateEmbed
     *         {@code true} to add information about the state description
     */
    void print(@Nonnull ConfigWizardSession session, boolean addStateEmbed);

    /**
     * @return the state that determines this service
     */
    ConfigWizardState getState();
}
