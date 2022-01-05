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
public class ConfigWizardVkProviderGroupIdSetter implements ConfigWizardVkProviderSetter {

    private static final String VARIABLE_NAME = "groupId";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderGroupIdSetter.class);

    private final DiscordProviderFromVkService vkProviderService;

    @Autowired
    public ConfigWizardVkProviderGroupIdSetter(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider) {
        int groupId;
        try {
            groupId = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build()
            );
        }

        provider.setGroupId(groupId);
        provider = vkProviderService.save(provider);

        logger.debug("The vkProvider={id={}} value 'groupId' is set to '{}'", provider.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `groupId` has been set to `" + value + "`")
                .build()
        );
    }

    @Nonnull
    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
