package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Component
public class ConfigWizardVkProviderBackCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderBackCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    @Autowired
    public ConfigWizardVkProviderBackCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Optional<DiscordProviderFromVk> vkProviderOptional = vkProviderService.findById(session.getEntityId());
        if (!vkProviderOptional.isPresent()) {
            // todo error response

            logger.error("execute(): Unable to get vkProvider={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }

        session.setEntityId(vkProviderOptional.get().getCategory().getId());
        return ConfigWizardState.VK_PROVIDERS;
    }
}
