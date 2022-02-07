package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.ConfigWizardVkProviderDescriptionService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderSetter;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
public class ConfigWizardVkProviderSetCommand implements ConfigWizardVkProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderSetCommand.class);

    private static final String NAME = "set";

    private final DiscordProviderFromVkService vkProviderService;
    private final ConfigWizardVkProviderDescriptionService descriptionService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardVkProviderSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardVkProviderSetCommand(DiscordProviderFromVkService vkProviderService,
                                            ConfigWizardVkProviderDescriptionService descriptionService,
                                            List<ConfigWizardVkProviderSetter> setters) {
        this.vkProviderService = vkProviderService;
        this.descriptionService = descriptionService;

        setters.forEach(setter -> this.setters.put(setter.getVariableName(), setter));
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

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
        ConfigWizardVkProviderSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }

        Optional<DiscordProviderFromVk> vkProviderOptional = vkProviderService.findById(entityId);
        if (vkProviderOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get vkProvider={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordProviderFromVk provider = vkProviderOptional.get();

        String value = parts.getOther();
        List<MessageEmbed> setResponses = setter.set(value, provider);
        responses.addAll(setResponses);

        responses.add(descriptionService.getDescription(provider));

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
