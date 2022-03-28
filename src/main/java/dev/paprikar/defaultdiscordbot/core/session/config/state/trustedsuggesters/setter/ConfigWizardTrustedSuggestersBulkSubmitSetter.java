package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.validation.ConfigWizardBooleanValidator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;

/**
 * The trusted suggesters bulk submit setter in a configuration session.
 */
@Component
public class ConfigWizardTrustedSuggestersBulkSubmitSetter implements ConfigWizardTrustedSuggestersSetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardTrustedSuggestersBulkSubmitSetter.class);

    private static final String VARIABLE_NAME = "bulkSubmit";

    private final DiscordCategoryService categoryService;
    private final ConfigWizardBooleanValidator validator;

    /**
     * Constructs a setter.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param validator
     *         an instance of {@link ConfigWizardBooleanValidator}
     */
    @Autowired
    public ConfigWizardTrustedSuggestersBulkSubmitSetter(DiscordCategoryService categoryService,
                                                         ConfigWizardBooleanValidator validator) {
        this.categoryService = categoryService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordCategory category) {
        DiscordValidatorProcessingResponse<Boolean> response = validator.process(value);
        Boolean isBulkSubmit = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }
        assert isBulkSubmit != null;

        category.setBulkSubmit(isBulkSubmit);
        category = categoryService.save(category);

        logger.debug("The category={id={}} value '{}' is set to {}", category.getId(), VARIABLE_NAME, value);

        return List.of(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The value `" + VARIABLE_NAME + "` has been set to `" + value + "`")
                .build());
    }

    @Override
    public String getVariableName() {
        return VARIABLE_NAME;
    }
}
