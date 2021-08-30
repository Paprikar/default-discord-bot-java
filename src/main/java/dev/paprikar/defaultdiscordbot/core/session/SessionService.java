package dev.paprikar.defaultdiscordbot.core.session;

import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.IConfigWizard;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final DiscordGuildService guildService;

    private final Map<ConfigWizardState, IConfigWizard> configWizardServices = new HashMap<>();

    // Map<InitiatorUserId, Session>
    private final Map<Long, PrivateSession> activePrivateSessions = new ConcurrentHashMap<>();

    @Autowired
    public SessionService(DiscordGuildService guildService,
                          List<IConfigWizard> configWizards) {
        this.guildService = guildService;
        for (IConfigWizard s : configWizards) {
            this.configWizardServices.put(s.getState(), s);
        }
    }

    public void handlePrivateMessage(PrivateMessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        PrivateSession session = activePrivateSessions.get(userId);
        if (session == null) {
            return;
        }
        IConfigWizard service = session.getService();
        ConfigWizardState targetState = service.handle(event, session);
        if (targetState == ConfigWizardState.END) {
            service.print(session, false);
            activePrivateSessions.remove(userId);
            session.getChannel().flatMap(PrivateChannel::close).queue();
            logger.debug("Configuration session is ended: userId={}", userId);
            return;
        }
        if (targetState == null) {
            service.print(session, false);
        } else {
            service = configWizardServices.get(targetState);
            session.setService(service);
            service.print(session, true);
        }
    }

    public void startConfigWizardSession(GuildMessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        // Ignore if any session is already started
        if (activePrivateSessions.containsKey(userId)) {
            // todo response ?
            return;
        }
        IConfigWizard initialService = configWizardServices.get(ConfigWizardState.ROOT);

        PrivateSession session = new PrivateSession();
        session.setChannel(event.getAuthor().openPrivateChannel());
        session.setService(initialService);
        session.setEntityId(guildService.getGuildByDiscordId(event.getGuild().getIdLong()).getId());
        activePrivateSessions.put(userId, session);

        initialService.print(session, true);
    }
}
