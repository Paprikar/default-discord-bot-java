package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.validation.ConfigWizardTimeValidator;
import dev.paprikar.defaultdiscordbot.utils.DateTimeConversions;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.sql.Time;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * The category start time setter in a configuration session.
 */
@Component
public class ConfigWizardCategoryStartTimeSetter implements ConfigWizardCategorySetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryStartTimeSetter.class);

    private static final String VARIABLE_NAME = "startTime";

    private final DiscordCategoryService categoryService;
    private final MediaActionService mediaActionService;
    private final SendingService sendingService;
    private final ConfigWizardTimeValidator validator;

    /**
     * Constructs a setter.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param mediaActionService an instance of {@link MediaActionService}
     * @param sendingService an instance of {@link SendingService}
     * @param validator an instance of {@link ConfigWizardTimeValidator}
     */
    @Autowired
    public ConfigWizardCategoryStartTimeSetter(DiscordCategoryService categoryService,
                                               MediaActionService mediaActionService,
                                               SendingService sendingService,
                                               ConfigWizardTimeValidator validator) {
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

        DiscordValidatorProcessingResponse<Time> response = validator.process(value);
        Time timeLocal = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }
        assert timeLocal != null;

        ZoneId zoneId = category.getGuild().getZoneId();
        Time timeUtc = Time.valueOf(
                DateTimeConversions.convertLocalTimeForZones(timeLocal.toLocalTime(), zoneId, ZoneOffset.UTC));
        category.setStartTime(timeUtc);
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

        logger.debug("The category={id={}} value '{}' is set to {}", category.getId(), VARIABLE_NAME, timeUtc);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `" + VARIABLE_NAME + "` has been set to `" + timeLocal + "`")
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
