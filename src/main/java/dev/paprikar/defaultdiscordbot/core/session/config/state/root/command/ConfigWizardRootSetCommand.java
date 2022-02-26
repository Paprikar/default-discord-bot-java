package dev.paprikar.defaultdiscordbot.core.session.config.state.root.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.setter.ConfigWizardRootSetter;
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
 * The command for setting guild variables.
 */
@Component
public class ConfigWizardRootSetCommand implements ConfigWizardRootCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootSetCommand.class);

    private static final String NAME = "set";

    private final DiscordGuildService guildService;

    // Map<VariableName, Setter>
    private final Map<String, ConfigWizardRootSetter> setters = new HashMap<>();

    /**
     * Constructs the command.
     *
     * @param guildService
     *         an instance of {@link DiscordGuildService}
     * @param setters
     *         a {@link List} of instances of {@link ConfigWizardRootSetter}
     */
    @Autowired
    public ConfigWizardRootSetCommand(DiscordGuildService guildService,
                                      List<ConfigWizardRootSetter> setters) {
        this.guildService = guildService;

        setters.forEach(setter -> this.setters.put(setter.getVariableName(), setter));
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
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
        ConfigWizardRootSetter setter = setters.get(varName);
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

        Optional<DiscordGuild> guildOptional = guildService.findById(entityId);
        if (guildOptional.isEmpty()) {
            logger.warn("execute(): Unable to get guild={id={}} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordGuild guild = guildOptional.get();

        String value = parts.getOther();
        List<MessageEmbed> setResponses = setter.set(value, guild);
        responses.addAll(setResponses);

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
