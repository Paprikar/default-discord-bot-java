package dev.paprikar.defaultdiscordbot.core.session.config.state.root;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.command.ConfigWizardRootOpenCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.command.ConfigWizardRootSetCommand;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigWizardRootService extends AbstractConfigWizard {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardRootService.class);

    private final DiscordGuildService guildService;

    private final ConfigWizardRootSetCommand setCommand;

    private final ConfigWizardRootOpenCommand openCommand;

    @Autowired
    public ConfigWizardRootService(DiscordGuildService guildService,
                                   ConfigWizardRootSetCommand setCommand,
                                   ConfigWizardRootOpenCommand openCommand) {
        super();

        this.guildService = guildService;

        this.setCommand = setCommand;
        this.openCommand = openCommand;

        setupCommands();
    }

    public static MessageEmbed getStateEmbed(DiscordGuild guild) {
        EmbedBuilder builder = new EmbedBuilder();

        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`prefix` = `" + guild.getPrefix() + "`\n\n");

        builder.appendDescription("Directories:\n");
        builder.appendDescription("`categories`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`open` `<directory>`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }

    @Nullable
    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull PrivateSession session) {
        logger.trace("handle(): event={}, sessionInfo={}", event, session);

        String message = event.getMessage().getContentRaw();
        FirstWordAndOther parts = new FirstWordAndOther(message);
        String commandName = parts.getFirstWord().toLowerCase();
        String argsString = parts.getOther();

        ConfigWizardCommand command = commands.get(commandName);
        if (command == null) {
            // todo illegal command response ?
            return null;
        }
        return command.execute(event, session, argsString);
    }

    @Transactional
    @Override
    public void print(@Nonnull PrivateSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Optional<DiscordGuild> guildOptional = guildService.findById(session.getEntityId());
            MessageEmbed embed;
            if (guildOptional.isPresent()) {
                embed = getStateEmbed(guildOptional.get());
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get guild={id={}}", session.getEntityId());
            }
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel().flatMap(c -> c.sendMessageEmbeds(responses)).queue();
            session.setResponses(new ArrayList<>());
        }
    }

    @Nonnull
    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.ROOT;
    }

    private void setupCommands() {
        commands.put("set", setCommand);
        commands.put("open", openCommand);
    }
}
