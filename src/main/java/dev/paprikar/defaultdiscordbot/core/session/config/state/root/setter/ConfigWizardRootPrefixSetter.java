package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Component
public class ConfigWizardRootPrefixSetter implements ConfigWizardRootSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardRootPrefixSetter.class);

    private final DiscordGuildService guildService;

    @Autowired
    public ConfigWizardRootPrefixSetter(DiscordGuildService guildService) {
        this.guildService = guildService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordGuild guild) {
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
        guild = guildService.save(guild);

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
