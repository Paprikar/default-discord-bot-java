package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation.ConfigWizardVkProviderNameValidator;
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
 * The vk provider name setter in a configuration session.
 */
@Component
public class ConfigWizardVkProviderNameSetter implements ConfigWizardVkProviderSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderNameSetter.class);

    private static final String VARIABLE_NAME = "name";

    private final DiscordProviderFromVkService vkProviderService;
    private final ConfigWizardVkProviderNameValidator validator;

    /**
     * Constructs a setter.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param validator
     *         an instance of {@link ConfigWizardVkProviderNameValidator}
     */
    @Autowired
    public ConfigWizardVkProviderNameSetter(DiscordProviderFromVkService vkProviderService,
                                            ConfigWizardVkProviderNameValidator validator) {
        this.vkProviderService = vkProviderService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider) {
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
        provider = vkProviderService.save(provider);

        logger.debug("The vkProvider={id={}} value '{}' is set to '{}'", provider.getId(), VARIABLE_NAME, value);

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
