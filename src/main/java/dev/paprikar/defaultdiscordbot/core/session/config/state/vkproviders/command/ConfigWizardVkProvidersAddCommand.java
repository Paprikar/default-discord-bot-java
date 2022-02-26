package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation.ConfigWizardVkProviderNameValidator;
import dev.paprikar.defaultdiscordbot.core.session.config.validation.ConfigWizardValidatorProcessingResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * The command to add vk providers.
 */
@Component
public class ConfigWizardVkProvidersAddCommand implements ConfigWizardVkProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersAddCommand.class);

    private static final String NAME = "add";

    private final DiscordCategoryService categoryService;
    private final DiscordProviderFromVkService vkProviderService;
    private final ConfigWizardVkProviderNameValidator validator;

    /**
     * Constructs the command.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param validator
     *         an instance of {@link ConfigWizardVkProviderNameValidator}
     */
    @Autowired
    public ConfigWizardVkProvidersAddCommand(DiscordCategoryService categoryService,
                                             DiscordProviderFromVkService vkProviderService,
                                             ConfigWizardVkProviderNameValidator validator) {
        this.categoryService = categoryService;
        this.vkProviderService = vkProviderService;
        this.validator = validator;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        List<MessageEmbed> responses = session.getResponses();

        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing for privateSession={}", session);
            return ConfigWizardState.IGNORE;
        }

        Long categoryId = session.getEntityId();
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            logger.warn("execute(): Unable to get category={id={}} for privateSession={}", categoryId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordCategory category = categoryOptional.get();

        DiscordProviderFromVk provider = new DiscordProviderFromVk();

        provider.attach(category);

        ConfigWizardValidatorProcessingResponse<String> response = validator.process(argsString, provider);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return ConfigWizardState.KEEP;
        }

        provider.setName(name);
        provider = vkProviderService.save(provider);

        session.setEntityId(provider.getId());

        logger.debug("Add at VK_PROVIDERS: privateSession={}, name='{}'", session, name);

        return ConfigWizardState.VK_PROVIDER;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
