package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.ConfigWizardDiscordProviderService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter.ConfigWizardDiscordProviderSetter;
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
public class ConfigWizardDiscordProviderSetCommand implements ConfigWizardDiscordProviderCommand {

    private static final String NAME = "set";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderSetCommand.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardDiscordProviderSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardDiscordProviderSetCommand(
            DiscordProviderFromDiscordService discordProviderService,
            List<ConfigWizardDiscordProviderSetter> setters) {
        this.discordProviderService = discordProviderService;

        for (ConfigWizardDiscordProviderSetter s : setters) {
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
        ConfigWizardDiscordProviderSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService
                .findById(session.getEntityId());
        if (discordProviderOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordProviderFromDiscord provider = discordProviderOptional.get();

        String value = parts.getOther();
        ConfigWizardSetterResponse response = setter.set(value, provider);
        session.getResponses().add(response.getEmbed());

        session.getResponses().add(ConfigWizardDiscordProviderService.getStateEmbed(provider));

        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
