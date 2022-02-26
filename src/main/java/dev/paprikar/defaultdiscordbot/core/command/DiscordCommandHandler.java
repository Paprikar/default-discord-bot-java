package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The service for handling discord commands.
 */
@Service
public class DiscordCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(DiscordCommandHandler.class);

    private final DiscordGuildService guildService;

    private final Map<String, DiscordCommand> commands = new HashMap<>();

    /**
     * Constructs a discord command handler.
     *
     * @param guildService
     *         an instance of {@link DiscordGuildService}
     * @param commands
     *         a {@link List} of instances of {@link DiscordCommand}
     */
    @Autowired
    public DiscordCommandHandler(DiscordGuildService guildService, List<DiscordCommand> commands) {
        this.guildService = guildService;

        commands.forEach(command -> this.commands.put(command.getName(), command));
    }

    /**
     * Handles events of type {@link GuildMessageReceivedEvent}.
     *
     * @param event
     *         the event of type {@link GuildMessageReceivedEvent} for handling
     */
    public void handleGuildMessageReceivedEvent(@Nonnull GuildMessageReceivedEvent event) {
        long guildId = event.getGuild().getIdLong();
        String message = event.getMessage().getContentRaw();

        Optional<DiscordGuild> guildOptional = guildService.findByDiscordId(guildId);
        if (guildOptional.isEmpty()) {
            return;
        }
        DiscordGuild guild = guildOptional.get();

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

        logger.debug("handleGuildMessageReceivedEvent(): command='{}' args='{}'", commandName, argsString);

        DiscordCommand command = commands.get(commandName);
        if (command != null) {
            command.execute(event, argsString);
        }
    }
}
