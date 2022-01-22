package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class ConfigWizardVkProvidersOpenCommand implements ConfigWizardVkProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersOpenCommand.class);

    private static final String NAME = "open";

    private final DiscordProviderFromVkService vkProviderService;

    @Autowired
    public ConfigWizardVkProvidersOpenCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        List<DiscordProviderFromVk> providers = vkProviderService.findAllByCategoryId(session.getEntityId());
        // todo use name index ?
        DiscordProviderFromVk targetProvider = null;
        for (DiscordProviderFromVk p : providers) {
            if (p.getName().equals(argsString)) {
                targetProvider = p;
                break;
            }
        }
        if (targetProvider == null) {
            // todo illegal command response
            return null;
        }

        session.setEntityId(targetProvider.getId());

        logger.debug("Open at VK_PROVIDERS: target='{}', session={}", argsString, session);

        return ConfigWizardState.VK_PROVIDER;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
