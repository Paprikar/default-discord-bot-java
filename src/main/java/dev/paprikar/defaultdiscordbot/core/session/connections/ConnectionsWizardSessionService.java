package dev.paprikar.defaultdiscordbot.core.session.connections;

import dev.paprikar.defaultdiscordbot.core.session.DiscordPrivateSession;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing connections sessions.
 */
@Service
public class ConnectionsWizardSessionService extends DiscordPrivateSession {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsWizardSessionService.class);

    private final Map<ConnectionsWizardState, ConnectionsWizard> connectionsWizardServices = new HashMap<>();

    // Map<InitiatorUserId, PrivateSession>
    private final Map<Long, ConnectionsWizardSession> activePrivateSessions = new ConcurrentHashMap<>();

    /**
     * Constructs the service.
     *
     * @param connectionsWizards a {@link List} of instances of {@link ConnectionsWizard}
     */
    @Autowired
    public ConnectionsWizardSessionService(List<ConnectionsWizard> connectionsWizards) {
        connectionsWizards.forEach(service -> this.connectionsWizardServices.put(service.getState(), service));
    }

    /**
     * Handles events of type {@link PrivateMessageReceivedEvent}.
     *
     * @param event the event of type {@link PrivateMessageReceivedEvent} for handling
     */
    public void handlePrivateMessageReceivedEvent(@Nonnull PrivateMessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        ConnectionsWizardSession session = activePrivateSessions.get(userId);
        if (session == null) {
            return;
        }

        ConnectionsWizard service = session.getService();
        ConnectionsWizardState targetState = service.handle(event, session);

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

                session.getChannel()
                        .close()
                        .queue(null, executionErrorHandler);

                logger.debug("handlePrivateMessageReceivedEvent(): "
                        + "Connections session is finished: privateSession={}", session);
                break;
            default:
                service = connectionsWizardServices.get(targetState);
                session.setService(service);
                service.print(session, true);
        }
    }

    /**
     * Handles events of type {@link GuildMessageReceivedEvent}.
     *
     * @param event the event of type {@link GuildMessageReceivedEvent} for handling
     */
    public void handleGuildMessageReceivedEvent(@Nonnull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }

        long userId = event.getAuthor().getIdLong();
        ConnectionsWizardSession session = activePrivateSessions.get(userId);
        if (session != null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Connections Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("Connections session is already started")
                    .build());

            session.getService().print(session, true);

            return;
        }

        if (activeUsers.contains(userId)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Connections Wizard")
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

        ConnectionsWizard initialService = connectionsWizardServices.get(ConnectionsWizardState.ROOT);

        try {
            session = new ConnectionsWizardSession(member, initialService);
        } catch (RuntimeException e) {
            logger.error("An error occurred while creating the session", e);
            return;
        }

        activeUsers.add(userId);
        activePrivateSessions.put(userId, session);

        initialService.print(session, true);
    }
}
