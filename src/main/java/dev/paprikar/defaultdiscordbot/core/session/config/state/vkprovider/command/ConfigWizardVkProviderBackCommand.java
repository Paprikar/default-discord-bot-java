package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * The command for switching from vk provider directory to vk providers directory.
 */
@Component
public class ConfigWizardVkProviderBackCommand implements ConfigWizardVkProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderBackCommand.class);

    private static final String NAME = "back";

    private final DiscordProviderFromVkService vkProviderService;

    /**
     * Constructs the command.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     */
    @Autowired
    public ConfigWizardVkProviderBackCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();

        Optional<DiscordProviderFromVk> vkProviderOptional = vkProviderService.findById(entityId);
        if (vkProviderOptional.isEmpty()) {
            logger.warn("execute(): Unable to get vkProvider={id={}} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
        }

        session.setEntityId(vkProviderOptional.get().getCategory().getId());
        return ConfigWizardState.VK_PROVIDERS;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
