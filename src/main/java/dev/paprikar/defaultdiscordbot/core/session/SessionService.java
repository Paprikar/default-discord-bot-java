package dev.paprikar.defaultdiscordbot.core.session;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final DiscordGuildService guildService;

    private final Map<ConfigWizardState, ConfigWizard> configWizardServices = new HashMap<>();

    // Map<InitiatorUserId, Session>
    private final Map<Long, PrivateSession> activePrivateSessions = new ConcurrentHashMap<>();

    // Set<DiscordGuildId>
    private final Set<Long> activeGuilds = ConcurrentHashMap.newKeySet();

    @Autowired
    public SessionService(DiscordGuildService guildService, List<ConfigWizard> configWizards) {
        this.guildService = guildService;

        for (ConfigWizard s : configWizards) {
            this.configWizardServices.put(s.getState(), s);
        }
    }

    public void handle(PrivateMessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        PrivateSession session = activePrivateSessions.get(userId);
        if (session == null) {
            return;
        }

        ConfigWizard service = session.getService();
        ConfigWizardState targetState = service.handle(event, session);

        if (targetState == ConfigWizardState.END) {
            service.print(session, false);

            activePrivateSessions.remove(userId);
            activeGuilds.remove(session.getDiscordGuildId());

            session.getChannel().flatMap(PrivateChannel::close).queue();
            logger.debug("handle(): Configuration session is ended: userId={}", userId);
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

    public void handle(GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        long userId = event.getAuthor().getIdLong();
        // Ignore if any session is already started
        if (activePrivateSessions.containsKey(userId)) {
            // todo response ?
            return;
        }

        long discordGuildId = event.getGuild().getIdLong();
        if (!activeGuilds.add(discordGuildId)) {
            // todo session is already active response
            return;
        }

        Optional<DiscordGuild> guildOptional = guildService.findByDiscordId(discordGuildId);
        if (!guildOptional.isPresent()) {
            return;
        }

        ConfigWizard initialService = configWizardServices.get(ConfigWizardState.ROOT);

        PrivateSession session = new PrivateSession(event.getAuthor().openPrivateChannel(), initialService,
                guildOptional.get().getId(), discordGuildId);

        activePrivateSessions.put(userId, session);

        initialService.print(session, true);
    }
}
