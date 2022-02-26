package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSessionService;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * The command for starting a guild configuration session.
 */
@Component
public class DiscordConfigCommand implements DiscordCommand {

    private static final String NAME = "config";

    private final ConfigWizardSessionService configWizardSessionService;

    /**
     * Constructs the command.
     *
     * @param configWizardSessionService
     *         an instance of {@link ConfigWizardSessionService}
     */
    @Autowired
    public DiscordConfigCommand(ConfigWizardSessionService configWizardSessionService) {
        this.configWizardSessionService = configWizardSessionService;
    }

    @Override
    public void execute(@Nonnull GuildMessageReceivedEvent event, @Nonnull String argsString) {
        configWizardSessionService.handleGuildMessageReceivedEvent(event);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
