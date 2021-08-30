package dev.paprikar.defaultdiscordbot.core.session.config.state.category.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.ConfigWizardCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter.*;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ConfigWizardCategorySetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategorySetCommand.class);

    private final DiscordCategoryService categoryService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardCategorySetter> setters = new HashMap<>();

    public ConfigWizardCategorySetCommand(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
        setupSetters();
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
        String value = parts.getOther();
        DiscordCategory category = categoryService.getCategoryById(session.getEntityId());

        ConfigWizardSetterResponse response = setter.set(value, category, categoryService);

        session.getResponses().add(response.getEmbed());
        session.getResponses().add(ConfigWizardCategoryService.getStateEmbed(category));

        return null;
    }

    private void setupSetters() {
        setters.put("name", new ConfigWizardCategoryNameSetter());
        setters.put("sendingChannelId", new ConfigWizardCategorySendingChannelIdSetter());
        setters.put("approvalChannelId", new ConfigWizardCategoryApprovalChannelIdSetter());
        setters.put("startTime", new ConfigWizardCategoryStartTimeSetter());
        setters.put("endTime", new ConfigWizardCategoryEndTimeSetter());
        setters.put("reserveDays", new ConfigWizardCategoryReserveDaysSetter());
        setters.put("positiveApprovalEmoji", new ConfigWizardCategoryPositiveApprovalEmojiSetter());
        setters.put("negativeApprovalEmoji", new ConfigWizardCategoryNegativeApprovalEmojiSetter());
    }
}
