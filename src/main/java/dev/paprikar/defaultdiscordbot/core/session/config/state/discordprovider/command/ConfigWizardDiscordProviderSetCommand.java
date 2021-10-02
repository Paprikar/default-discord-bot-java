package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.ConfigWizardDiscordProviderService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter.ConfigWizardDiscordProviderSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter.ConfigWizardDiscordProviderSuggestionChannelIdSetter;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter.ConfigWizardDiscordProviderNameSetter;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ConfigWizardDiscordProviderSetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderSetCommand.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardDiscordProviderSetter> setters = new HashMap<>();

    public ConfigWizardDiscordProviderSetCommand(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
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
        ConfigWizardDiscordProviderSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }
        String value = parts.getOther();
        DiscordProviderFromDiscord provider = discordProviderService.getById(session.getEntityId());

        ConfigWizardSetterResponse response = setter.set(value, provider, discordProviderService);

        session.getResponses().add(response.getEmbed());
        session.getResponses().add(ConfigWizardDiscordProviderService.getStateEmbed(provider));

        return null;
    }

    private void setupSetters() {
        setters.put("name", new ConfigWizardDiscordProviderNameSetter());
        setters.put("suggestionChannelId", new ConfigWizardDiscordProviderSuggestionChannelIdSetter());
    }
}
