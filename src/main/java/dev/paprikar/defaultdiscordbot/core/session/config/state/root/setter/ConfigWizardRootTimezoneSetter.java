package dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.validation.ConfigWizardZoneIdValidator;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * The guild timezone setter in a configuration session.
 */
@Component
public class ConfigWizardRootTimezoneSetter implements ConfigWizardRootSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootTimezoneSetter.class);

    private static final String VARIABLE_NAME = "timezone";

    private final DiscordGuildService guildService;
    private final ConfigWizardZoneIdValidator validator;

    /**
     * Constructs a setter.
     *
     * @param guildService an instance of {@link DiscordGuildService}
     * @param validator an instance of {@link ConfigWizardZoneIdValidator}
     */
    @Autowired
    public ConfigWizardRootTimezoneSetter(DiscordGuildService guildService,
                                          ConfigWizardZoneIdValidator validator) {
        this.guildService = guildService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordGuild guild) {
        DiscordValidatorProcessingResponse<ZoneId> response = validator.process(value);
        ZoneId zoneId = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }

        guild.setZoneId(zoneId);
        guild = guildService.save(guild);

        logger.debug("The guild={id={}} value '{}' is set to '{}'", guild.getId(), VARIABLE_NAME, value);

        return List.of(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `" + VARIABLE_NAME + "` has been set to `" + value + "`")
                .build());
    }

    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
