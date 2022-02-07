package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

@Service
public class ConfigWizardVkProvidersDescriptionService {

    public MessageEmbed getDescription(@Nonnull DiscordCategory category,
                                       @Nonnull List<DiscordProviderFromVk> providers) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "/vk providers`\n\n");

        if (!providers.isEmpty()) {
            builder.appendDescription("Vk providers:\n");
            providers.stream()
                    .map(provider -> "`" + provider.getName() + "`\n")
                    .forEach(builder::appendDescription);
            builder.appendDescription("\n");
        }

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`open` `<name>`\n");
        builder.appendDescription("`add` `<name>`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
