package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardVkProviderTokenSetter implements ConfigWizardVkProviderSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderTokenSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordProviderFromVk provider,
                                          @Nonnull DiscordProviderFromVkService vkProviderService) {
        provider.setName(value);
        vkProviderService.saveProvider(provider);
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
