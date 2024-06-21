package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation.ConfigWizardVkProviderCredsValidator;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation.ConfigWizardVkProviderGroupIdValidator;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The vk provider group id setter in a configuration session.
 */
@Component
public class ConfigWizardVkProviderGroupIdSetter implements ConfigWizardVkProviderSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderGroupIdSetter.class);

    private static final String VARIABLE_NAME = "groupId";

    private final DiscordProviderFromVkService vkProviderService;
    private final MediaActionService mediaActionService;
    private final VkSuggestionService vkSuggestionService;
    private final ConfigWizardVkProviderGroupIdValidator validator;
    private final ConfigWizardVkProviderCredsValidator credsValidator;

    /**
     * Constructs a setter.
     *
     * @param vkProviderService an instance of {@link DiscordProviderFromVkService}
     * @param mediaActionService an instance of {@link MediaActionService}
     * @param vkSuggestionService an instance of {@link VkSuggestionService}
     * @param validator an instance of {@link ConfigWizardVkProviderGroupIdValidator}
     * @param credsValidator an instance of {@link ConfigWizardVkProviderCredsValidator}
     */
    @Autowired
    public ConfigWizardVkProviderGroupIdSetter(DiscordProviderFromVkService vkProviderService,
                                               MediaActionService mediaActionService,
                                               VkSuggestionService vkSuggestionService,
                                               ConfigWizardVkProviderGroupIdValidator validator,
                                               ConfigWizardVkProviderCredsValidator credsValidator) {
        this.vkProviderService = vkProviderService;
        this.mediaActionService = mediaActionService;
        this.vkSuggestionService = vkSuggestionService;
        this.validator = validator;
        this.credsValidator = credsValidator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromVk provider) {
        DiscordValidatorProcessingResponse<Integer> response = validator.process(value);
        Integer groupId = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }
        assert groupId != null;

        List<MessageEmbed> responses = new ArrayList<>();
        boolean enabledResponse = false;

        if (provider.isEnabled()) {
            error = credsValidator.test(groupId, provider.getToken());
            if (error != null) {
                responses.add(error);
                return responses;
            }

            provider.setGroupId(groupId);
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
            provider.setGroupId(groupId);
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
