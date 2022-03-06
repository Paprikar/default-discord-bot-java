package dev.paprikar.defaultdiscordbot.core.session.config;

/**
 * The configuration session states.
 */
public enum ConfigWizardState {

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
     * The state of the session in initial (guild) directory.
     */
    ROOT,

    /**
     * The state of the session in categories directory.
     */
    CATEGORIES,

    /**
     * The state of the session in trusted suggesters directory.
     */
    TRUSTED_SUGGESTERS,

    /**
     * The state of the session in category directory.
     */
    CATEGORY,

    /**
     * The state of the session in discord providers directory.
     */
    DISCORD_PROVIDERS,

    /**
     * The state of the session in discord provider directory.
     */
    DISCORD_PROVIDER,

    /**
     * The state of the session in vk providers directory.
     */
    VK_PROVIDERS,

    /**
     * The state of the session in vk provider directory.
     */
    VK_PROVIDER,
}
