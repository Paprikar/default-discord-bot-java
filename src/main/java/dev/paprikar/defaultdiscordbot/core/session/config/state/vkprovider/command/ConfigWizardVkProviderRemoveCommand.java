package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The command for removing a vk provider.
 */
@Component
public class ConfigWizardVkProviderRemoveCommand implements ConfigWizardVkProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderRemoveCommand.class);

    private static final String NAME = "remove";

    private final DiscordProviderFromVkService vkProviderService;

    /**
     * Constructs the command.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     */
    @Autowired
    public ConfigWizardVkProviderRemoveCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromVk> vkProviderOptional = vkProviderService.findById(entityId);
        if (vkProviderOptional.isEmpty()) {
            logger.warn("execute(): Unable to get vkProvider={id={} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromVk provider = vkProviderOptional.get();

        if (provider.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider that is enabled cannot be deleted")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        session.setEntityId(provider.getCategory().getId());
        vkProviderService.delete(provider);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The provider `" + provider.getName() + "` has been successfully deleted")
                .build()
        );

        logger.debug("The vkProvider={id={}} was deleted", provider.getId());

        return ConfigWizardState.VK_PROVIDERS;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
