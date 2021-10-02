package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardRootPrefixSetter implements ConfigWizardRootSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardRootPrefixSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordGuildService guildService,
                                          @Nonnull DiscordGuild guild) {
        if (value.length() > 32) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the prefix cannot be more than 32 characters")
                    .build()
            );
        }
        guild.setPrefix(value);
        guildService.save(guild);
        logger.debug("The guild={id={}} prefix is set to '{}'", guild.getId(), value);
        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Prefix value has been set to `" + value + "`")
                .build()
        );
    }
}
