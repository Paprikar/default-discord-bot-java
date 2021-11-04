package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
public class ConfigWizardCategoryStartTimeSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryStartTimeSetter.class);

    private final DiscordCategoryService categoryService;

    private final SendingService sendingService;

    @Autowired
    public ConfigWizardCategoryStartTimeSetter(DiscordCategoryService categoryService,
                                               SendingService sendingService) {
        this.categoryService = categoryService;
        this.sendingService = sendingService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category) {
        Time time;
        try {
            time = Time.valueOf(LocalTime.parse(value));
        } catch (DateTimeParseException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Value has an invalid format")
                    .build()
            );
        }

        // todo checks

        category.setStartTime(time);
        category = categoryService.save(category);

        sendingService.updateCategory(category);

        logger.debug("The category={id={}} startTime is set to '{}'", category.getId(), time);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("StartTime value has been set to `" + time + "`")
                .build()
        );
    }
}
