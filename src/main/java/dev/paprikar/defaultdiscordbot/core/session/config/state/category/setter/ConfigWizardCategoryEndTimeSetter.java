package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation.ConfigWizardCategoryTimeValidator;
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
import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigWizardCategoryEndTimeSetter implements ConfigWizardCategorySetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryEndTimeSetter.class);

    private static final String VARIABLE_NAME = "endTime";

    private final DiscordCategoryService categoryService;
    private final MediaActionService mediaActionService;
    private final SendingService sendingService;
    private final ConfigWizardCategoryTimeValidator validator;

    @Autowired
    public ConfigWizardCategoryEndTimeSetter(DiscordCategoryService categoryService,
                                             MediaActionService mediaActionService,
                                             SendingService sendingService,
                                             ConfigWizardCategoryTimeValidator validator) {
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

        ConfigWizardValidatorProcessingResponse<Time> response = validator.process(value);
        Time time = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }

        category.setEndTime(time);
        category = categoryService.save(category);

        List<MessageEmbed> responses = new ArrayList<>();
        boolean enabledResponse = false;

        if (category.isEnabled()) {
            if (sendingService.contains(category)) {
                sendingService.update(category);
            } else {
                List<MessageEmbed> errors = mediaActionService.enableSending(category, jda);
                if (errors.isEmpty()) {
                    enabledResponse = true;
                }
            }
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
