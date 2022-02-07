package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.validation.ConfigWizardRootPrefixValidator;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

@Component
public class ConfigWizardRootPrefixSetter implements ConfigWizardRootSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootPrefixSetter.class);

    private static final String VARIABLE_NAME = "prefix";

    private final DiscordGuildService guildService;
    private final ConfigWizardRootPrefixValidator validator;

    @Autowired
    public ConfigWizardRootPrefixSetter(DiscordGuildService guildService, ConfigWizardRootPrefixValidator validator) {
        this.guildService = guildService;
        this.validator = validator;
    }

    @Override
    public MessageEmbed set(@Nonnull String value, @Nonnull DiscordGuild guild) {
        ConfigWizardValidatorProcessingResponse<String> response = validator.process(value);
        String prefix = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return error;
        }

        guild.setPrefix(prefix);
        guild = guildService.save(guild);

        logger.debug("The guild={id={}} value '{}' is set to '{}'", guild.getId(), VARIABLE_NAME, value);

        return new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `" + VARIABLE_NAME + "` has been set to `" + value + "`")
                .build();
    }

    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
