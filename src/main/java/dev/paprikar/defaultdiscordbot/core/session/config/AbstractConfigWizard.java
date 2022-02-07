package dev.paprikar.defaultdiscordbot.core.session.config;

import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardExitCommand;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
import dev.paprikar.defaultdiscordbot.utils.JdaUtils.RequestErrorHandler;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfigWizard implements ConfigWizard {

    // Map<CommandName, Command>
    protected final Map<String, ConfigWizardCommand> commands = new HashMap<>();

    protected RequestErrorHandler printingErrorHandler;

    protected AbstractConfigWizard() {
        setupCommands();

        this.printingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while printing the session responses")
                .build();
    }

    @Nullable
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull PrivateSession session) {
        String message = event.getMessage().getContentRaw();
        FirstWordAndOther parts = new FirstWordAndOther(message);
        String commandName = parts.getFirstWord().toLowerCase();

        ConfigWizardCommand command = commands.get(commandName);
        if (command == null) {
            // todo illegal command response ?
            return null;
        }

        String argsString = parts.getOther();
        return command.execute(event, session, argsString);
    }

    private void setupCommands() {
        ConfigWizardExitCommand exitCommand = new ConfigWizardExitCommand();
        commands.put(exitCommand.getName(), exitCommand);
    }
}
