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
 * The command for enabling a vk provider.
 */
@Component
public class ConfigWizardVkProviderEnableCommand implements ConfigWizardVkProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderEnableCommand.class);

    private static final String NAME = "enable";

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
    public ConfigWizardVkProviderEnableCommand(DiscordProviderFromVkService vkProviderService,
                                               MediaActionService mediaActionService) {
        this.vkProviderService = vkProviderService;
        this.mediaActionService = mediaActionService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long providerId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromVk> providerOptional = vkProviderService.findById(providerId);
        if (providerOptional.isEmpty()) {
            logger.warn("execute(): Unable to get vkProvider={id={}} for privateSession={}", providerId, session);
            return ConfigWizardState.IGNORE;
        }
        DiscordProviderFromVk provider = providerOptional.get();

        if (provider.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider is already enabled")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        provider.setEnabled(true);
        provider = vkProviderService.save(provider);

        if (provider.getCategory().isEnabled()) {
            List<MessageEmbed> errors = mediaActionService.enableVkProvider(provider);
            responses.addAll(errors);

            if (errors.isEmpty()) {
                responses.add(new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTitle("Configuration Wizard")
                        .setTimestamp(Instant.now())
                        .appendDescription("The provider has been enabled")
                        .build());
            }
        } else {
            responses.add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("The flag `enabled` has been set")
                    .build());
        }

        return ConfigWizardState.KEEP;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
