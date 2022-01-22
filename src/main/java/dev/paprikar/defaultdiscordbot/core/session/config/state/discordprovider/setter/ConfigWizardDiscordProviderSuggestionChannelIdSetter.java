package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardDiscordTextChannelIdValidator;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
public class ConfigWizardDiscordProviderSuggestionChannelIdSetter implements ConfigWizardDiscordProviderSetter {

    private static final Logger logger = LoggerFactory.getLogger(
            ConfigWizardDiscordProviderSuggestionChannelIdSetter.class);

    private static final String VARIABLE_NAME = "suggestionChannelId";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final MediaActionService mediaActionService;
    private final DiscordSuggestionService discordSuggestionService;
    private final JDAService jdaService;
    private final ConfigWizardDiscordTextChannelIdValidator validator;

    @Autowired
    public ConfigWizardDiscordProviderSuggestionChannelIdSetter(
            DiscordProviderFromDiscordService discordProviderService,
            MediaActionService mediaActionService,
            DiscordSuggestionService discordSuggestionService,
            JDAService jdaService,
            ConfigWizardDiscordTextChannelIdValidator validator) {
        this.discordProviderService = discordProviderService;
        this.mediaActionService = mediaActionService;
        this.discordSuggestionService = discordSuggestionService;
        this.jdaService = jdaService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordProviderFromDiscord provider) {
        JDA jda = jdaService.get();
        if (jda == null) {
            logger.warn("set(): Failed to get jda");
            return List.of(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The operation was not performed due to internal errors")
                    .build());
        }

        ConfigWizardValidatorProcessingResponse<Long> response = validator.process(value);
        Long newChannelId = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }

        List<MessageEmbed> responses = new ArrayList<>();
        boolean enabledResponse = false;

        if (provider.isEnabled()) {
            error = validator.test(newChannelId, provider.getCategory().getGuild().getDiscordId(), jda);
            if (error != null) {
                responses.add(error);
                return responses;
            }

            long oldChannelId = provider.getSuggestionChannelId();
            provider.setSuggestionChannelId(newChannelId);
            provider = discordProviderService.save(provider);

            if (discordSuggestionService.contains(provider)) {
                discordSuggestionService.update(oldChannelId, newChannelId);
            } else {
                List<MessageEmbed> errors = mediaActionService.enableDiscordProvider(provider);
                if (errors.isEmpty()) {
                    enabledResponse = true;
                }
            }
        } else {
            provider.setSuggestionChannelId(newChannelId);
            provider = discordProviderService.save(provider);
        }

        logger.debug("The discordProvider={id={}} value '{}' is set to '{}'", provider.getId(), VARIABLE_NAME, value);

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
