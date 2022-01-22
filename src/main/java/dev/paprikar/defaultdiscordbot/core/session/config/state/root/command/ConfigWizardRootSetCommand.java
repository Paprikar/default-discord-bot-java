package dev.paprikar.defaultdiscordbot.core.session.config.state.root.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.ConfigWizardRootService;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter.ConfigWizardRootSetter;
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
public class ConfigWizardRootSetCommand implements ConfigWizardRootCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootSetCommand.class);

    private static final String NAME = "set";

    private final DiscordGuildService guildService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardRootSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardRootSetCommand(DiscordGuildService guildService, List<ConfigWizardRootSetter> setters) {
        this.guildService = guildService;

        for (ConfigWizardRootSetter s : setters) {
            this.setters.put(s.getVariableName(), s);
        }
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
        ConfigWizardRootSetter setter = setters.get(varName);
        if (setter == null) {
            // todo illegal var name response
            return null;
        }

        Optional<DiscordGuild> guildOptional = guildService.findById(entityId);
        if (guildOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get guild={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordGuild guild = guildOptional.get();

        String value = parts.getOther();
        MessageEmbed response = setter.set(value, guild);
        responses.add(response);

        responses.add(ConfigWizardRootService.getStateEmbed(guild));

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
