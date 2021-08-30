package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigWizardVkProviderBackCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderBackCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    public ConfigWizardVkProviderBackCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("onExitCommand(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        session.setEntityId(vkProviderService.getProviderById(session.getEntityId()).getCategory().getId());
        return ConfigWizardState.VK_PROVIDERS;
    }
}
