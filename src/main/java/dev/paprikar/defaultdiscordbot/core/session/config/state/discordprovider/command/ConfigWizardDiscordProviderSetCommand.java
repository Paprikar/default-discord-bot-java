package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.ConfigWizardDiscordProviderDescriptionService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter.ConfigWizardDiscordProviderSetter;
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
public class ConfigWizardDiscordProviderSetCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderSetCommand.class);

    private static final String NAME = "set";

    private final DiscordProviderFromDiscordService discordProviderService;
    private final ConfigWizardDiscordProviderDescriptionService descriptionService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardDiscordProviderSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardDiscordProviderSetCommand(DiscordProviderFromDiscordService discordProviderService,
                                                 ConfigWizardDiscordProviderDescriptionService descriptionService,
                                                 List<ConfigWizardDiscordProviderSetter> setters) {
        this.discordProviderService = discordProviderService;
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
        ConfigWizardDiscordProviderSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService.findById(entityId);
        if (discordProviderOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordProviderFromDiscord provider = discordProviderOptional.get();

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
