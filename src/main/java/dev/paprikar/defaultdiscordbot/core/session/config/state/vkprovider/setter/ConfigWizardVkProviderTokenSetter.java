package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation.ConfigWizardVkProviderCredsValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigWizardVkProviderTokenSetter implements ConfigWizardVkProviderSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderTokenSetter.class);

    private static final String VARIABLE_NAME = "token";

    private final DiscordProviderFromVkService vkProviderService;
    private final MediaActionService mediaActionService;
    private final VkSuggestionService vkSuggestionService;
    private final ConfigWizardVkProviderCredsValidator credsValidator;

    @Autowired
    public ConfigWizardVkProviderTokenSetter(DiscordProviderFromVkService vkProviderService,
                                             MediaActionService mediaActionService,
                                             VkSuggestionService vkSuggestionService,
                                             ConfigWizardVkProviderCredsValidator credsValidator) {
        this.vkProviderService = vkProviderService;
        this.mediaActionService = mediaActionService;
        this.vkSuggestionService = vkSuggestionService;
        this.credsValidator = credsValidator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider) {
        List<MessageEmbed> responses = new ArrayList<>();
        boolean enabledResponse = false;

        if (provider.isEnabled()) {
            MessageEmbed error = credsValidator.test(provider.getGroupId(), value);
            if (error != null) {
                responses.add(error);
                return responses;
            }

            provider.setToken(value);
            provider = vkProviderService.save(provider);

            if (vkSuggestionService.contains(provider)) {
                vkSuggestionService.update(provider);
            } else {
                List<MessageEmbed> errors = mediaActionService.enableVkProvider(provider);
                if (errors.isEmpty()) {
                    enabledResponse = true;
                }
            }
        } else {
            provider.setToken(value);
            provider = vkProviderService.save(provider);
        }

        logger.debug("The vkProvider={id={}} value '{}' is set to '{}'", provider.getId(), VARIABLE_NAME, value);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `" + VARIABLE_NAME + "` has been set to `" + value + "`")
                .build());

        if (enabledResponse) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Provider was enabled")
                    .build());
        }

        return responses;
    }

    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
