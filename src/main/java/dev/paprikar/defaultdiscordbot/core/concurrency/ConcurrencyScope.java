package dev.paprikar.defaultdiscordbot.core.concurrency;

public enum ConcurrencyScope {

    // Key: CategoryId
    CATEGORY_SENDING_CONFIGURATION,

    // Key: CategoryId
    CATEGORY_APPROVE_CONFIGURATION,

    // Key: SuggestionChannelId
    CATEGORY_DISCORD_PROVIDER_CONFIGURATION,

    // Key: CategoryId
    CATEGORY_APPROVE,
}
