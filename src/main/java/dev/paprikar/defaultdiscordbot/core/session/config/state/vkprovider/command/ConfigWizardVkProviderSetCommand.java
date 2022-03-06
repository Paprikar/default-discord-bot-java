package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.setter.ConfigWizardVkProviderSetter;
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

/**
 * The command for setting vk provider variables.
 */
@Component
public class ConfigWizardVkProviderSetCommand implements ConfigWizardVkProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderSetCommand.class);

    private static final String NAME = "set";

    private final DiscordProviderFromVkService vkProviderService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardVkProviderSetter> setters = new HashMap<>();

    /**
     * Constructs the command.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param setters
     *         a {@link List} of instances of {@link ConfigWizardVkProviderSetter}
     */
    @Autowired
    public ConfigWizardVkProviderSetCommand(DiscordProviderFromVkService vkProviderService,
                                            List<ConfigWizardVkProviderSetter> setters) {
        this.vkProviderService = vkProviderService;

        setters.forEach(setter -> this.setters.put(setter.getVariableName(), setter));
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        Long providerId = session.getEntityId();
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
        ConfigWizardVkProviderSetter setter = setters.get(varName);
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

        Optional<DiscordProviderFromVk> vkProviderOptional = vkProviderService.findById(providerId);
        if (vkProviderOptional.isEmpty()) {
            logger.warn("execute(): Unable to get vkProvider={id={}} for privateSession={}", providerId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromVk provider = vkProviderOptional.get();

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
