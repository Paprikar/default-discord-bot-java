package dev.paprikar.defaultdiscordbot.core.session.config;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.DiscordPrivateSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing configuration sessions.
 */
@Service
public class ConfigWizardSessionService extends DiscordPrivateSession {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardSessionService.class);

    private final DiscordGuildService guildService;

    private final Map<ConfigWizardState, ConfigWizard> configWizardServices = new HashMap<>();

    // Map<UserId, PrivateSession>
    private final Map<Long, ConfigWizardSession> activePrivateSessions = new ConcurrentHashMap<>();

    // Set<GuildDiscordId>
    private final Set<Long> activeGuilds = ConcurrentHashMap.newKeySet();

    /**
     * Constructs the service.
     *
     * @param guildService
     *         an instance of {@link DiscordGuildService}
     * @param configWizards
     *         a {@link List} of instances of {@link ConfigWizard}
     */
    @Autowired
    public ConfigWizardSessionService(DiscordGuildService guildService, List<ConfigWizard> configWizards) {
        this.guildService = guildService;

        configWizards.forEach(service -> this.configWizardServices.put(service.getState(), service));
    }

    /**
     * Handles events of type {@link PrivateMessageReceivedEvent}.
     *
     * @param event
     *         the event of type {@link PrivateMessageReceivedEvent} for handling
     */
    public void handlePrivateMessageReceivedEvent(PrivateMessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        ConfigWizardSession session = activePrivateSessions.get(userId);
        if (session == null) {
            return;
        }

        ConfigWizard service = session.getService();
        ConfigWizardState targetState = service.handle(event, session);

        switch (targetState) {
            case IGNORE:
                break;
            case KEEP:
                service.print(session, true);
                break;
            case END:
                service.print(session, false);

                activeUsers.remove(userId);
                activePrivateSessions.remove(userId);
                activeGuilds.remove(session.getGuildDiscordId());

                session.getChannel()
                        .close()
                        .queue(null, executionErrorHandler);

                logger.debug("handlePrivateMessageReceivedEvent(): "
                        + "Configuration session is finished: privateSession={}", session);
                break;
            default:
                service = configWizardServices.get(targetState);
                session.setService(service);
                service.print(session, true);
        }
    }

    /**
     * Handles events of type {@link GuildMessageReceivedEvent}.
     *
     * @param event
     *         the event of type {@link GuildMessageReceivedEvent} for handling
     */
    public void handleGuildMessageReceivedEvent(GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        long userId = event.getAuthor().getIdLong();
        ConfigWizardSession session = activePrivateSessions.get(userId);
        if (session != null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Configuration session is already started")
                    .build());

            session.getService().print(session, true);

            return;
        }

        if (activeUsers.contains(userId)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Another session is already started")
                    .build();

            event.getAuthor().openPrivateChannel()
                    .flatMap(channel -> channel
                            .sendMessageEmbeds(embed)
                            .reference(event.getMessage())
                            .and(channel.close()))
                    .queue(null, executionErrorHandler);

            return;
        }

        long guildDiscordId = event.getGuild().getIdLong();
        if (activeGuilds.contains(guildDiscordId)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Another configuration session is taking place now")
                    .build();

            event.getAuthor().openPrivateChannel()
                    .flatMap(channel -> channel
                            .sendMessageEmbeds(embed)
                            .reference(event.getMessage())
                            .and(channel.close()))
                    .queue(null, executionErrorHandler);

            return;
        }

        Optional<DiscordGuild> guildOptional = guildService.findByDiscordId(guildDiscordId);
        if (guildOptional.isEmpty()) {
            return;
        }
        Long guildId = guildOptional.get().getId();

        ConfigWizard initialService = configWizardServices.get(ConfigWizardState.ROOT);

        try {
            session = new ConfigWizardSession(event.getMember(), initialService, guildId);
        } catch (RuntimeException e) {
            logger.error("An error occurred while creating the session", e);
            return;
        }

        activeUsers.add(userId);
        activePrivateSessions.put(userId, session);
        activeGuilds.add(guildDiscordId);

        initialService.print(session, true);
    }
}
