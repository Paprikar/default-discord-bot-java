package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider;

import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Service
public class ConfigWizardVkProviderDescriptionService {

    private final VkSuggestionService vkSuggestionService;

    @Autowired
    public ConfigWizardVkProviderDescriptionService(VkSuggestionService vkSuggestionService) {
        this.vkSuggestionService = vkSuggestionService;
    }

    public MessageEmbed getDescription(@Nonnull DiscordProviderFromVk provider) {
        DiscordCategory category = provider.getCategory();
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() +
                "/vk providers/" + provider.getName() + "`\n\n");

        String currentState = vkSuggestionService.contains(provider) ? "enabled" : "disabled";
        builder.appendDescription("Current state: `" + currentState + "`\n\n");

        String savedState = provider.isEnabled() ? "enabled" : "disabled";
        builder.appendDescription("Saved state: `" + savedState + "`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + provider.getName() + "`\n");
        builder.appendDescription("`groupId` = `" + provider.getGroupId() + "`\n");
        builder.appendDescription("`token` = `" + provider.getToken() + "`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`enable`\n");
        builder.appendDescription("`disable`\n");
        builder.appendDescription("`remove`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
