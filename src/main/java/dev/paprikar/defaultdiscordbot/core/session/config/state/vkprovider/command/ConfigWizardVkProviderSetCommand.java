package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.ConfigWizardVkProviderService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderTokenSetter;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderNameSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderSetter;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ConfigWizardVkProviderSetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderSetCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardVkProviderSetter> setters = new HashMap<>();

    public ConfigWizardVkProviderSetCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
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
        ConfigWizardVkProviderSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }
        String value = parts.getOther();
        DiscordProviderFromVk provider = vkProviderService.getProviderById(session.getEntityId());

        ConfigWizardSetterResponse response = setter.set(value, provider, vkProviderService);

        session.getResponses().add(response.getEmbed());
        session.getResponses().add(ConfigWizardVkProviderService.getStateEmbed(provider));

        return null;
    }

    private void setupSetters() {
        setters.put("name", new ConfigWizardVkProviderNameSetter());
        setters.put("token", new ConfigWizardVkProviderTokenSetter());
    }
}
