package dev.paprikar.defaultdiscordbot.core.session.config;

import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardExitCommand;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import dev.paprikar.defaultdiscordbot.utils.JdaUtils.RequestErrorHandler;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for configuration state services.
 */
public abstract class ConfigWizard {

    /**
     * Map to get configuration commands by their aliases.
     */
    // Map<CommandName, Command>
    protected final Map<String, ConfigWizardCommand> commands = new HashMap<>();

    /**
     * Error handler for sending responses.
     */
    protected final RequestErrorHandler printingErrorHandler;

    /**
     * Constructs a configuration state service.
     */
    public ConfigWizard() {
        setupCommands();

        this.printingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while printing the session responses")
                .build();
    }

    /**
     * Handles private messages within a configuration session.
     *
     * @param event
     *         the event of type {@link PrivateMessageReceivedEvent} for handling
     * @param session
     *         the configuration session
     *
     * @return the state of type {@link ConfigWizardState} after handling
     */
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull ConfigWizardSession session) {
        String message = event.getMessage().getContentRaw();
        FirstWordAndOther parts = new FirstWordAndOther(message);
        String commandName = parts.getFirstWord().toLowerCase();

        ConfigWizardCommand command = commands.get(commandName);
        if (command == null) {
            return ConfigWizardState.IGNORE;
        }

        String argsString = parts.getOther();
        return command.execute(event, session, argsString);
    }

    /**
     * Sends a response as part of a configuration session.
     *
     * @param session
     *         the configuration session
     * @param addStateEmbed
     *         {@code true} to add information about the state description
     */
    public abstract void print(@Nonnull ConfigWizardSession session, boolean addStateEmbed);

    /**
     * @return the state that determines this service
     */
    public abstract ConfigWizardState getState();

    private void setupCommands() {
        ConfigWizardExitCommand exitCommand = new ConfigWizardExitCommand();
        commands.put(exitCommand.getName(), exitCommand);
    }
}
