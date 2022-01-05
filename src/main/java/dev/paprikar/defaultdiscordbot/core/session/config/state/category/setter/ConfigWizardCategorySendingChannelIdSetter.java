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
public class ConfigWizardCategorySendingChannelIdSetter implements ConfigWizardCategorySetter {

    private static final String VARIABLE_NAME = "sendingChannelId";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategorySendingChannelIdSetter.class);

    private final DiscordCategoryService categoryService;

    private final SendingService sendingService;

    @Autowired
    public ConfigWizardCategorySendingChannelIdSetter(DiscordCategoryService categoryService,
                                                      SendingService sendingService) {
        this.categoryService = categoryService;
        this.sendingService = sendingService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category) {
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The value has an invalid format")
                    .build()
            );
        }

        // todo checks

        category.setSendingChannelId(id);
        category = categoryService.save(category);

        sendingService.update(category);

        logger.debug("The category={id={}} value 'sendingChannelId' is set to '{}'", category.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `sendingChannelId` has been set to `" + value + "`")
                .build()
        );
    }

    @Nonnull
    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
