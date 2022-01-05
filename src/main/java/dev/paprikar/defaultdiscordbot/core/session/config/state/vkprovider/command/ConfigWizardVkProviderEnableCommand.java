package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.ConfigWizardVkProviderService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ConfigWizardVkProviderEnableCommand implements ConfigWizardVkProviderCommand {

    private static final String NAME = "enable";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderEnableCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    private final MediaActionService mediaActionService;

    @Autowired
    public ConfigWizardVkProviderEnableCommand(DiscordProviderFromVkService vkProviderService,
                                               MediaActionService mediaActionService) {
        this.vkProviderService = vkProviderService;
        this.mediaActionService = mediaActionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("execute(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        Long entityId = session.getEntityId();

        Optional<DiscordProviderFromVk> providerOptional = vkProviderService.findById(entityId);
        if (providerOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get vkProvider={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordProviderFromVk provider = providerOptional.get();

        List<MessageEmbed> responses = session.getResponses();

        if (provider.isEnabled()) {
            // todo already enabled response

            responses.add(ConfigWizardVkProviderService.getStateEmbed(provider));

            return null;
        }

        provider.setEnabled(true);
        provider = vkProviderService.save(provider);

        if (provider.getCategory().isEnabled()) {
            List<MessageEmbed> errors = mediaActionService.enableVkProvider(provider);

            if (errors.isEmpty()) {
                responses.add(new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTitle("Configuration Wizard")
                        .setTimestamp(Instant.now())
                        .appendDescription("The provider has been enabled")
                        .build());
            } else {
                responses.addAll(errors);
            }
        } else {
            responses.add(new EmbedBuilder()
                    .setColor(Color.GRAY)
                    .setTitle("Configuration Wizard")
                    .setTimestamp(Instant.now())
                    .appendDescription("The flag `enabled` has been set")
                    .build());
        }

        responses.add(ConfigWizardVkProviderService.getStateEmbed(provider));

        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
