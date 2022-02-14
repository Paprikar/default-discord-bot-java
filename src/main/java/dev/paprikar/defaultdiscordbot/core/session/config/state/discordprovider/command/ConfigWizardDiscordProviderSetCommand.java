package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.setter.ConfigWizardDiscordProviderSetter;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ConfigWizardDiscordProviderSetCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderSetCommand.class);

    private static final String NAME = "set";

    private final DiscordProviderFromDiscordService discordProviderService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardDiscordProviderSetter> setters = new HashMap<>();

    @Autowired
    public ConfigWizardDiscordProviderSetCommand(DiscordProviderFromDiscordService discordProviderService,
                                                 List<ConfigWizardDiscordProviderSetter> setters) {
        this.discordProviderService = discordProviderService;

        setters.forEach(setter -> this.setters.put(setter.getVariableName(), setter));
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing for privateSession={}", session);
            return ConfigWizardState.IGNORE;
        }

        if (argsString.isEmpty()) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name of variable cannot be empty")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        FirstWordAndOther parts = new FirstWordAndOther(argsString);
        String varName = parts.getFirstWord();
        ConfigWizardDiscordProviderSetter setter = setters.get(varName);
        if (setter == null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The variable with the name `" + varName + "` does not exist")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService.findById(entityId);
        if (discordProviderOptional.isEmpty()) {
            logger.warn("execute(): Unable to get discordProvider={id={}} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromDiscord provider = discordProviderOptional.get();

        String value = parts.getOther();
        List<MessageEmbed> setResponses = setter.set(value, provider);
        responses.addAll(setResponses);

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
