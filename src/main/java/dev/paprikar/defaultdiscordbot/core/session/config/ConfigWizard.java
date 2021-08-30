package dev.paprikar.defaultdiscordbot.core.session.config;

import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardExitCommand;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigWizard implements IConfigWizard {

    // Map<CommandName, Command>
    protected final Map<String, ConfigWizardCommand> commands = new HashMap<>();

    protected ConfigWizard() {
        setupCommands();
    }

    private void setupCommands() {
        commands.put("exit", new ConfigWizardExitCommand());
    }
}
