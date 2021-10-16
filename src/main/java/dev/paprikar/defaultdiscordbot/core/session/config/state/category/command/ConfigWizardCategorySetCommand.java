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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ConfigWizardCategorySetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategorySetCommand.class);

    private final DiscordCategoryService categoryService;

    private final ConfigWizardCategoryNameSetter nameSetter;

    private final ConfigWizardCategorySendingChannelIdSetter sendingChannelIdSetter;

    private final ConfigWizardCategoryApprovalChannelIdSetter approvalChannelIdSetter;

    private final ConfigWizardCategoryStartTimeSetter startTimeSetter;

    private final ConfigWizardCategoryEndTimeSetter endTimeSetter;

    private final ConfigWizardCategoryReserveDaysSetter reserveDaysSetter;

    private final ConfigWizardCategoryPositiveApprovalEmojiSetter positiveApprovalEmojiSetter;

    private final ConfigWizardCategoryNegativeApprovalEmojiSetter negativeApprovalEmojiSetter;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardCategorySetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardCategorySetCommand(DiscordCategoryService categoryService,
                                          ConfigWizardCategoryNameSetter nameSetter,
                                          ConfigWizardCategorySendingChannelIdSetter sendingChannelIdSetter,
                                          ConfigWizardCategoryApprovalChannelIdSetter approvalChannelIdSetter,
                                          ConfigWizardCategoryStartTimeSetter startTimeSetter,
                                          ConfigWizardCategoryEndTimeSetter endTimeSetter,
                                          ConfigWizardCategoryReserveDaysSetter reserveDaysSetter,
                                          ConfigWizardCategoryPositiveApprovalEmojiSetter positiveApprovalEmojiSetter,
                                          ConfigWizardCategoryNegativeApprovalEmojiSetter negativeApprovalEmojiSetter) {
        this.categoryService = categoryService;

        this.nameSetter = nameSetter;
        this.sendingChannelIdSetter = sendingChannelIdSetter;
        this.approvalChannelIdSetter = approvalChannelIdSetter;
        this.startTimeSetter = startTimeSetter;
        this.endTimeSetter = endTimeSetter;
        this.reserveDaysSetter = reserveDaysSetter;
        this.positiveApprovalEmojiSetter = positiveApprovalEmojiSetter;
        this.negativeApprovalEmojiSetter = negativeApprovalEmojiSetter;

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

        Optional<DiscordCategory> categoryOptional = categoryService.findById(session.getEntityId());
        if (!categoryOptional.isPresent()) {
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

    private void setupSetters() {
        setters.put("name", nameSetter);
        setters.put("sendingChannelId", sendingChannelIdSetter);
        setters.put("approvalChannelId", approvalChannelIdSetter);
        setters.put("startTime", startTimeSetter);
        setters.put("endTime", endTimeSetter);
        setters.put("reserveDays", reserveDaysSetter);
        setters.put("positiveApprovalEmoji", positiveApprovalEmojiSetter);
        setters.put("negativeApprovalEmoji", negativeApprovalEmojiSetter);
    }
}
