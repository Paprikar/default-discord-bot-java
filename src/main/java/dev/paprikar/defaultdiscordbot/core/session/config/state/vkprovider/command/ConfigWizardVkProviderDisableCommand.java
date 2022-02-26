package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
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
 * The command for disabling a vk provider.
 */
@Component
public class ConfigWizardVkProviderDisableCommand implements ConfigWizardVkProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderDisableCommand.class);

    private static final String NAME = "disable";

    private final DiscordProviderFromVkService vkProviderService;
    private final MediaActionService mediaActionService;

    /**
     * Constructs the command.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param mediaActionService
     *         an instance of {@link MediaActionService}
     */
    @Autowired
    public ConfigWizardVkProviderDisableCommand(DiscordProviderFromVkService vkProviderService,
                                                MediaActionService mediaActionService) {
        this.vkProviderService = vkProviderService;
        this.mediaActionService = mediaActionService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromVk> providerOptional = vkProviderService.findById(entityId);
        if (providerOptional.isEmpty()) {
            logger.warn("execute(): Unable to get discordProvider={id={}} for privateSession={}", entityId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromVk provider = providerOptional.get();

        if (!provider.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider is already disabled")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        provider.setEnabled(false);
        provider = vkProviderService.save(provider);

        String message;
        if (provider.getCategory().isEnabled()) {
            mediaActionService.disableVkProvider(provider);
            message = "The provider has been disabled";
        } else {
            message = "The flag `enabled` has been unset";
        }
        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription(message)
                .build());

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
