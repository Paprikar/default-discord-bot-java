package dev.paprikar.defaultdiscordbot.core.session.config.state.root;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.command.ConfigWizardRootCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for the configuration state of initial directory.
 */
@Service
public class ConfigWizardRootService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootService.class);

    private final DiscordGuildService guildService;

    /**
     * Constructs a configuration state service.
     *
     * @param guildService
     *         an instance of {@link DiscordGuildService}
     * @param commands
     *         a {@link List} of instances of {@link ConfigWizardRootCommand}
     */
    @Autowired
    public ConfigWizardRootService(DiscordGuildService guildService,
                                   List<ConfigWizardRootCommand> commands) {
        super();

        this.guildService = guildService;

        commands.forEach(command -> this.commands.put(command.getName(), command));
    }

    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull ConfigWizardSession session) {
        logger.trace("handle(): privateSession={}", session);

        return super.handle(event, session);
    }

    @Transactional
    @Override
    public void print(@Nonnull ConfigWizardSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Long guildId = session.getEntityId();
            Optional<DiscordGuild> guildOptional = guildService.findById(guildId);
            if (guildOptional.isEmpty()) {
                logger.error("print(): Unable to get guild={id={}} for privateSession={}", guildId, session);
                return;
            }

            MessageEmbed embed = getDescription(guildOptional.get());
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel()
                    .sendMessageEmbeds(responses)
                    .queue(null, printingErrorHandler);
            session.setResponses(new ArrayList<>());
        }
    }

    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.ROOT;
    }

    private MessageEmbed getDescription(@Nonnull DiscordGuild guild) {
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
}
