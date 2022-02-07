package dev.paprikar.defaultdiscordbot.core.concurrency;

public enum ConcurrencyScope {

    // Key: CategoryId
    CATEGORY_SENDING_CONFIGURATION,

    // Key: CategoryId
    CATEGORY_APPROVE_CONFIGURATION,

    // Key: ProviderId
    CATEGORY_PROVIDER_FROM_DISCORD_CONFIGURATION,

    // Key: ProviderId
    CATEGORY_PROVIDER_FROM_VK_CONFIGURATION,

    // Key: CategoryId
    CATEGORY_APPROVE,
}
