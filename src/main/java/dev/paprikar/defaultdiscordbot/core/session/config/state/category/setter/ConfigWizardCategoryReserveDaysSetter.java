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
import java.time.Instant;

@Component
public class ConfigWizardCategoryReserveDaysSetter implements ConfigWizardCategorySetter {

    private static final String VARIABLE_NAME = "reserveDays";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryReserveDaysSetter.class);

    private final DiscordCategoryService categoryService;

    private final SendingService sendingService;

    @Autowired
    public ConfigWizardCategoryReserveDaysSetter(DiscordCategoryService categoryService,
                                                 SendingService sendingService) {
        this.categoryService = categoryService;
        this.sendingService = sendingService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category) {
        int reserveDays;
        try {
            reserveDays = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build()
            );
        }

        if (reserveDays < 1) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value `reserveDays` can only be positive")
                    .build()
            );
        }

        category.setReserveDays(reserveDays);
        category = categoryService.save(category);

        sendingService.update(category);

        logger.debug("The category={id={}} value 'reserveDays' is set to '{}'", category.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `reserveDays` has been set to `" + value + "`")
                .build()
        );
    }

    @Nonnull
    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
