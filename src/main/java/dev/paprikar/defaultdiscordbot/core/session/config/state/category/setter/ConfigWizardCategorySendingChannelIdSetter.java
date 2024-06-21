package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.validation.ConfigWizardDiscordTextChannelIdValidator;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
 * The category sending channel id setter in a configuration session.
 */
@Component
public class ConfigWizardCategorySendingChannelIdSetter implements ConfigWizardCategorySetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategorySendingChannelIdSetter.class);

    private static final String VARIABLE_NAME = "sendingChannelId";

    private final DiscordCategoryService categoryService;
    private final MediaActionService mediaActionService;
    private final SendingService sendingService;
    private final ConfigWizardDiscordTextChannelIdValidator validator;

    /**
     * Constructs a setter.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param mediaActionService an instance of {@link MediaActionService}
     * @param sendingService an instance of {@link SendingService}
     * @param validator an instance of {@link ConfigWizardDiscordTextChannelIdValidator}
     */
    @Autowired
    public ConfigWizardCategorySendingChannelIdSetter(DiscordCategoryService categoryService,
                                                      MediaActionService mediaActionService,
                                                      SendingService sendingService,
                                                      ConfigWizardDiscordTextChannelIdValidator validator) {
        this.categoryService = categoryService;
        this.mediaActionService = mediaActionService;
        this.sendingService = sendingService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordCategory category) {
        JDA jda = JDAService.get();
        if (jda == null) {
            logger.error("set(): Failed to get jda");
            return List.of(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The operation was not performed due to internal errors")
                    .build());
        }

        DiscordValidatorProcessingResponse<Long> response = validator.process(value);
        Long channelId = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }
        assert channelId != null;

        List<MessageEmbed> responses = new ArrayList<>();
        boolean enabledResponse = false;

        if (category.isEnabled()) {
            error = validator.test(channelId, category.getGuild().getDiscordId(), jda);
            if (error != null) {
                responses.add(error);
                return responses;
            }

            category.setSendingChannelId(channelId);
            category = categoryService.save(category);

            if (sendingService.contains(category)) {
                sendingService.update(category);
            } else {
                List<MessageEmbed> errors = mediaActionService.enableSending(category, jda);
                if (errors.isEmpty()) {
                    enabledResponse = true;
                }
            }
        } else {
            category.setSendingChannelId(channelId);
            category = categoryService.save(category);
        }

        logger.debug("The category={id={}} value '{}' is set to '{}'", category.getId(), VARIABLE_NAME, value);

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
                    .appendDescription("Sending module was enabled")
                    .build());
        }

        return responses;
    }

    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
