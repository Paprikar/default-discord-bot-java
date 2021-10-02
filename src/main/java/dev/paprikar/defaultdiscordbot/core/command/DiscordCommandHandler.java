package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.SessionService;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiscordCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(DiscordCommandHandler.class);

    private final DiscordGuildService guildService;

    private final SessionService sessionService;

    private final Map<String, DiscordCommand> commands = new HashMap<>();

    public DiscordCommandHandler(DiscordGuildService guildService, SessionService sessionService) {
        this.guildService = guildService;
        this.sessionService = sessionService;
        setupCommands();
    }

    public void handle(@Nonnull GuildMessageReceivedEvent event) {
        long guildId = event.getGuild().getIdLong();
        String message = event.getMessage().getContentRaw();

        DiscordGuild guild = guildService.getByDiscordId(guildId);
        String prefix = guild.getPrefix();
        if (!message.startsWith(prefix)) {
            return;
        }
        String argsString = message.substring(prefix.length());
        if (argsString.isEmpty()) {
            return;
        }
        FirstWordAndOther parts = new FirstWordAndOther(argsString);
        String commandName = parts.getFirstWord().toLowerCase();
        argsString = parts.getOther();

        logger.debug("handle(): command='{}' args='{}'", commandName, argsString);
        DiscordCommand command = commands.get(commandName);
        if (command != null) {
            command.execute(argsString, event);
        }
    }

    private void setupCommands() {
        commands.put("ping", new DiscordPingCommand());
        commands.put("config", new DiscordConfigCommand(sessionService));
    }
}
