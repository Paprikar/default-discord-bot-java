package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
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
public class ConfigWizardVkProviderTokenSetter implements ConfigWizardVkProviderSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderTokenSetter.class);

    private final DiscordProviderFromVkService vkProviderService;

    @Autowired
    public ConfigWizardVkProviderTokenSetter(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider) {
        provider.setName(value);
        provider = vkProviderService.save(provider);

        logger.debug("The vkProvider={id={}} token is set to '{}'", provider.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Token value has been set to `" + value + "`")
                .build()
        );
    }
}
