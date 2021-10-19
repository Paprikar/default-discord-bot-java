package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.ConfigWizardVkProviderService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderNameSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderTokenSetter;
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
public class ConfigWizardVkProviderSetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderSetCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    private final ConfigWizardVkProviderNameSetter nameSetter;

    private final ConfigWizardVkProviderTokenSetter tokenSetter;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardVkProviderSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardVkProviderSetCommand(DiscordProviderFromVkService vkProviderService,
                                            ConfigWizardVkProviderNameSetter nameSetter,
                                            ConfigWizardVkProviderTokenSetter tokenSetter) {
        this.vkProviderService = vkProviderService;

        this.nameSetter = nameSetter;
        this.tokenSetter = tokenSetter;

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

        Optional<DiscordProviderFromVk> vkProviderOptional = vkProviderService.findById(session.getEntityId());
        if (!vkProviderOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get vkProvider={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordProviderFromVk provider = vkProviderOptional.get();

        String value = parts.getOther();

        ConfigWizardSetterResponse response = setter.set(value, provider);

        session.getResponses().add(response.getEmbed());
        session.getResponses().add(ConfigWizardVkProviderService.getStateEmbed(provider));

        return null;
    }

    private void setupSetters() {
        setters.put("name", nameSetter);
        setters.put("token", tokenSetter);
    }
}
