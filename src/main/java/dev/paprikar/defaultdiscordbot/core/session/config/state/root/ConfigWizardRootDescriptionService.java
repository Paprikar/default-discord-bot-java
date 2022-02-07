package dev.paprikar.defaultdiscordbot.core.session.config.state.root;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Service
public class ConfigWizardRootDescriptionService {

    public MessageEmbed getDescription(@Nonnull DiscordGuild guild) {
        EmbedBuilder builder = new EmbedBuilder();

        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`prefix` = `" + guild.getPrefix() + "`\n\n");

        builder.appendDescription("Directories:\n");
        builder.appendDescription("`categories`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`open` `<directory>`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
