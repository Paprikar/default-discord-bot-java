package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.ConfigWizardCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter.ConfigWizardCategorySetter;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ConfigWizardCategorySetCommand implements ConfigWizardCategoryCommand {

    private static final String NAME = "set";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategorySetCommand.class);

    private final DiscordCategoryService categoryService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardCategorySetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardCategorySetCommand(DiscordCategoryService categoryService,
                                          List<ConfigWizardCategorySetter> setters) {
        this.categoryService = categoryService;

        for (ConfigWizardCategorySetter s : setters) {
            this.setters.put(s.getVariableName(), s);
        }
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing");
            // todo internal error response
            return null;
        }
        if (argsString.isEmpty()) {
            // todo illegal args response
            return null;
        }
        FirstWordAndOther parts = new FirstWordAndOther(argsString);
        String varName = parts.getFirstWord();
        ConfigWizardCategorySetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(session.getEntityId());
        if (categoryOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordCategory category = categoryOptional.get();

        String value = parts.getOther();
        ConfigWizardSetterResponse response = setter.set(value, category);
        session.getResponses().add(response.getEmbed());

        session.getResponses().add(ConfigWizardCategoryService.getStateEmbed(category));

        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
