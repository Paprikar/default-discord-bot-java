package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordValidatorProcessingResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation.ConfigWizardCategoryNameValidator;
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
 * The category name setter in a configuration session.
 */
@Component
public class ConfigWizardCategoryNameSetter implements ConfigWizardCategorySetter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryNameSetter.class);

    private static final String VARIABLE_NAME = "name";

    private final DiscordCategoryService categoryService;
    private final ConfigWizardCategoryNameValidator validator;

    /**
     * Constructs a setter.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param validator
     *         an instance of {@link ConfigWizardCategoryNameValidator}
     */
    @Autowired
    public ConfigWizardCategoryNameSetter(DiscordCategoryService categoryService,
                                          ConfigWizardCategoryNameValidator validator) {
        this.categoryService = categoryService;
        this.validator = validator;
    }

    @Override
    public List<MessageEmbed> set(@Nonnull String value, @Nonnull DiscordCategory category) {
        DiscordValidatorProcessingResponse<String> response = validator.process(value, category);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            return List.of(error);
        }
        assert name != null;

        error = validator.test(name, category.getGuild().getId());
        if (error != null) {
            return List.of(error);
        }

        category.setName(name);
        category = categoryService.save(category);

        logger.debug("The category={id={}} value '{}' is set to '{}'", category.getId(), VARIABLE_NAME, value);

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
