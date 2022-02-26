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
public abstract class AbstractConfigWizard implements ConfigWizard {

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
    protected AbstractConfigWizard() {
        setupCommands();

        this.printingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while printing the session responses")
                .build();
    }

    @Override
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

    private void setupCommands() {
        ConfigWizardExitCommand exitCommand = new ConfigWizardExitCommand();
        commands.put(exitCommand.getName(), exitCommand);
    }
}
