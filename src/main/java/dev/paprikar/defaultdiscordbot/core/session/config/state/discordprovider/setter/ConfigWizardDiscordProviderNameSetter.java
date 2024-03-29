package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.validation.ConfigWizardDiscordProviderNameValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

/**
 * The discord provider name setter in a configuration session.
 */
@Component
public class ConfigWizardDiscordProviderNameSetter implements ConfigWizardDiscordProviderSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderNameSetter.class);

    private static final String VARIABLE_NAME = "name";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final ConfigWizardDiscordProviderNameValidator validator;

    /**
     * Constructs a setter.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     * @param validator
     *         an instance of {@link ConfigWizardDiscordProviderNameValidator}
     */
    @Autowired
    public ConfigWizardDiscordProviderNameSetter(DiscordProviderFromDiscordService discordProviderService,
                                                 ConfigWizardDiscordProviderNameValidator validator) {
        this.discordProviderService = discordProviderService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromDiscord provider) {
        DiscordValidatorProcessingResponse<String> response = validator.process(value, provider);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }
        assert name != null;

        error = validator.test(name, provider.getCategory().getId());
        if (error != null) {
            return List.of(error);
        }

        provider.setName(name);
        provider = discordProviderService.save(provider);

        logger.debug("The discordProvider={id={}} value '{}' is set to '{}'", provider.getId(), VARIABLE_NAME, value);

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
