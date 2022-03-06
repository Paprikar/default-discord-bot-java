package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.session.connections.ConnectionsWizardSessionService;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * The command for starting a user connections session.
 */
@Component
public class DiscordConnectionsCommand implements DiscordCommand {

    private static final String NAME = "connections";

    private final ConnectionsWizardSessionService connectionsWizardSessionService;

    /**
     * Constructs the command.
     *
     * @param connectionsWizardSessionService
     *         an instance of {@link ConnectionsWizardSessionService}
     */
    @Autowired
    public DiscordConnectionsCommand(ConnectionsWizardSessionService connectionsWizardSessionService) {
        this.connectionsWizardSessionService = connectionsWizardSessionService;
    }

    @Override
    public void execute(@Nonnull GuildMessageReceivedEvent event, @Nonnull String argsString) {
        connectionsWizardSessionService.handleGuildMessageReceivedEvent(event);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
