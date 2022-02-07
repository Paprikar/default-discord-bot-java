package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
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
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Component
public class ConfigWizardVkProvidersAddCommand implements ConfigWizardVkProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersAddCommand.class);

    private static final String NAME = "add";

    private final DiscordCategoryService categoryService;
    private final DiscordProviderFromVkService vkProviderService;
    private final ConfigWizardVkProviderNameValidator validator;

    @Autowired
    public ConfigWizardVkProvidersAddCommand(DiscordCategoryService categoryService,
                                             DiscordProviderFromVkService vkProviderService,
                                             ConfigWizardVkProviderNameValidator validator) {
        this.categoryService = categoryService;
        this.vkProviderService = vkProviderService;
        this.validator = validator;
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

        Long categoryId = session.getEntityId();
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", categoryId);

            return ConfigWizardState.END;
        }
        DiscordCategory category = categoryOptional.get();

        DiscordProviderFromVk provider = new DiscordProviderFromVk();

        provider.attach(category);

        ConfigWizardValidatorProcessingResponse<String> response = validator.process(argsString, provider);
        String name = response.getValue();
        MessageEmbed error = response.getError();

        if (error != null) {
            responses.add(error);
            return null;
        }

        provider.setName(name);
        provider = vkProviderService.save(provider);

        session.setEntityId(provider.getId());

        logger.debug("Add at VK_PROVIDERS: name={}, session={}", name, session);

        return ConfigWizardState.VK_PROVIDER;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
