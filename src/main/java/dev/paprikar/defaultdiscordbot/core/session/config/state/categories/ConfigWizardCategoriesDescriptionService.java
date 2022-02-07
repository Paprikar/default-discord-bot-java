package dev.paprikar.defaultdiscordbot.core.session.config.state.categories;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

@Service
public class ConfigWizardCategoriesDescriptionService {

    public MessageEmbed getDescription(@Nonnull List<DiscordCategory> categories) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories`\n\n");

        if (!categories.isEmpty()) {
            builder.appendDescription("Categories:\n");
            categories.stream()
                    .map(category -> "`" + category.getName() + "`\n")
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
