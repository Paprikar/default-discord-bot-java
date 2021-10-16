package dev.paprikar.defaultdiscordbot.core.session.config.state.root.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.ConfigWizardRootService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter.ConfigWizardRootPrefixSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter.ConfigWizardRootSetter;
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
public class ConfigWizardRootSetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardRootSetCommand.class);

    private final DiscordGuildService guildService;

    private final ConfigWizardRootPrefixSetter prefixSetter;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardRootSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardRootSetCommand(DiscordGuildService guildService, ConfigWizardRootPrefixSetter prefixSetter) {
        this.guildService = guildService;

        this.prefixSetter = prefixSetter;

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
        ConfigWizardRootSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }

        Optional<DiscordGuild> guildOptional = guildService.findById(session.getEntityId());
        if (!guildOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get guild={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }
        DiscordGuild guild = guildOptional.get();

        String value = parts.getOther();

        ConfigWizardSetterResponse response = setter.set(value, guild);

        session.getResponses().add(response.getEmbed());
        session.getResponses().add(ConfigWizardRootService.getStateEmbed(guild));

        logger.debug("The guild={id={}} prefix is set to '{}'", session.getEntityId(), value);

        return null;
    }

    private void setupSetters() {
        setters.put("prefix", prefixSetter);
    }
}
