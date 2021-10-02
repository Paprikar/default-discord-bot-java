package dev.paprikar.defaultdiscordbot.core.session.config.state.root.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter.ConfigWizardRootPrefixSetter;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter.ConfigWizardRootSetter;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.ConfigWizardRootService;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ConfigWizardRootSetCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardRootSetCommand.class);

    private final DiscordGuildService guildService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardRootSetter> setters = new HashMap<>();

    public ConfigWizardRootSetCommand(DiscordGuildService guildService) {
        this.guildService = guildService;
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
        String value = parts.getOther();
        DiscordGuild guild = guildService.getById(session.getEntityId());
        ConfigWizardSetterResponse response = setter.set(value, guildService, guild);

        session.getResponses().add(response.getEmbed());
        session.getResponses().add(ConfigWizardRootService.getStateEmbed(guild));

        logger.debug("The guild={id={}} prefix is set to '{}'", session.getEntityId(), value);
        return null;
    }

    private void setupSetters() {
        setters.put("prefix", new ConfigWizardRootPrefixSetter());
    }
}
