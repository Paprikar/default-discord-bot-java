package dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command;

import dev.paprikar.defaultdiscordbot.config.DdbConfig;
import dev.paprikar.defaultdiscordbot.config.DdbDefaults;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.category.validation.ConfigWizardCategoryNameValidator;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Component
public class ConfigWizardCategoriesAddCommand implements ConfigWizardCategoriesCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesAddCommand.class);

    private static final String NAME = "add";

    private final DiscordGuildService guildService;
    private final DiscordCategoryService categoryService;
    private final ConfigWizardCategoryNameValidator validator;
    private final DdbConfig config;

    @Autowired
    public ConfigWizardCategoriesAddCommand(DiscordGuildService guildService,
                                            DiscordCategoryService categoryService,
                                            ConfigWizardCategoryNameValidator validator,
                                            DdbConfig config) {
        this.guildService = guildService;
        this.categoryService = categoryService;
        this.validator = validator;
        this.config = config;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        List<MessageEmbed> responses = session.getResponses();

        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing");
            // todo internal error response
            return null;
        }

        Long guildId = session.getEntityId();
        Optional<DiscordGuild> guildOptional = guildService.findById(guildId);
        if (guildOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get guild={id={}}, ending session", guildId);

            return ConfigWizardState.END;
        }
        DiscordGuild guild = guildOptional.get();

        DiscordCategory category = new DiscordCategory();
        DdbDefaults defaults = config.getDefaults();

        category.attach(guild);
        category.setPositiveApprovalEmoji(defaults.getPositiveApprovalEmoji());
        category.setNegativeApprovalEmoji(defaults.getNegativeApprovalEmoji());

        ConfigWizardValidatorProcessingResponse<String> response = validator.process(argsString, category);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return null;
        }

        category.setName(name);
        category = categoryService.save(category);

        session.setEntityId(category.getId());

        logger.debug("Add at CATEGORIES: name={}, session={}", name, session);

        return ConfigWizardState.CATEGORY;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
