package dev.paprikar.defaultdiscordbot.core.concurrency;

/**
 * The concurrency scopes.
 */
public enum ConcurrencyScope {

    /**
     * The scope of category sending module configuration.
     */
    // Key: CategoryId
    CATEGORY_SENDING_CONFIGURATION,

    /**
     * The scope of category approval module configuration.
     */
    // Key: CategoryId
    CATEGORY_APPROVE_CONFIGURATION,

    /**
     * The scope of category discord provider configuration.
     */
    // Key: ProviderId
    CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION,

    /**
     * The scope of category vk provider configuration.
     */
    // Key: ProviderId
    CATEGORY_PROVIDER_FROM_VK_CONFIGURATION,

    /**
     * The scope of category suggestions approval.
     */
    // Key: CategoryId
    CATEGORY_APPROVE,
}
