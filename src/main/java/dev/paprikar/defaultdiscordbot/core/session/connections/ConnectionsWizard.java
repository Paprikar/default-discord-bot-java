package dev.paprikar.defaultdiscordbot.core.session.connections;

import dev.paprikar.defaultdiscordbot.core.session.connections.command.ConnectionsWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.connections.command.ConnectionsWizardExitCommand;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for connections state services.
 */
public abstract class ConnectionsWizard {

    /**
     * Map to get commands by their aliases.
     */
    // Map<CommandName, Command>
    protected final Map<String, ConnectionsWizardCommand> commands = new HashMap<>();

    /**
     * Error handler for sending responses.
     */
    protected final RequestErrorHandler printingErrorHandler;

    /**
     * Constructs a connections state service.
     */
    public ConnectionsWizard() {
        setupCommands();

        this.printingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while printing the session responses")
                .build();
    }

    /**
     * Handles private messages within a connections session.
     *
     * @param event the event of type {@link PrivateMessageReceivedEvent} for handling
     * @param session the conenctions session
     *
     * @return the state of type {@link ConnectionsWizardState} after handling
     */
    public ConnectionsWizardState handle(@Nonnull PrivateMessageReceivedEvent event,
                                         @Nonnull ConnectionsWizardSession session) {
        String message = event.getMessage().getContentRaw();
        FirstWordAndOther parts = new FirstWordAndOther(message);
        String commandName = parts.getFirstWord().toLowerCase();

        ConnectionsWizardCommand command = commands.get(commandName);
        if (command == null) {
            return ConnectionsWizardState.IGNORE;
        }

        String argsString = parts.getOther();
        return command.execute(event, session, argsString);
    }

    /**
     * Sends a response as part of a connections session.
     *
     * @param session the conenctions session
     * @param addStateEmbed {@code true} to add information about the state description
     */
    public abstract void print(@Nonnull ConnectionsWizardSession session, boolean addStateEmbed);

    /**
     * @return the state that determines this service
     */
    public abstract ConnectionsWizardState getState();

    private void setupCommands() {
        ConnectionsWizardExitCommand exitCommand = new ConnectionsWizardExitCommand();
        commands.put(exitCommand.getName(), exitCommand);
    }
}
