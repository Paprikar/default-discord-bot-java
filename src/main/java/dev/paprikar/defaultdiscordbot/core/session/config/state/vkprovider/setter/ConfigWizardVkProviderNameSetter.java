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
import java.util.List;

public class ConfigWizardVkProviderNameSetter implements ConfigWizardVkProviderSetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderNameSetter.class);

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value,
                                          @Nonnull DiscordProviderFromVk provider,
                                          @Nonnull DiscordProviderFromVkService vkProviderService) {
        if (value.isEmpty()) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name cannot be empty")
                    .build()
            );
        }
        if (value.length() > 32) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build()
            );
        }
        if (provider.getName().equals(value)) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Changing vk provider name to the same one does not make sense")
                    .build()
            );
        }
        List<DiscordProviderFromVk> providers = vkProviderService
                .findProvidersByCategoryId(provider.getCategory().getId());
        for (DiscordProviderFromVk p : providers) {
            if (p.getName().equals(value)) {
                return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("Vk provider name must be unique")
                        .build()
                );
            }
        }
        provider.setName(value);
        vkProviderService.saveProvider(provider);
        logger.debug("The vkProvider={id={}} name is set to '{}'", provider.getId(), value);
        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Name value has been set to `" + value + "`")
                .build()
        );
    }
}
