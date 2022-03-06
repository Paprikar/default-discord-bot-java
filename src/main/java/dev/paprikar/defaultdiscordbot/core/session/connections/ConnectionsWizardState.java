package dev.paprikar.defaultdiscordbot.core.session.connections;

/**
 * The connections session states.
 */
public enum ConnectionsWizardState {

    /**
     * Ignores anything and, as a consequence, does nothing.
     */
    IGNORE,

    /**
     * Keeps the current state.
     */
    KEEP,

    /**
     * The final state of the session.
     */
    END,

    /**
     * The state of the session in initial directory.
     */
    ROOT,

    /**
     * The state of the session in vk directory.
     */
    VK,
}
