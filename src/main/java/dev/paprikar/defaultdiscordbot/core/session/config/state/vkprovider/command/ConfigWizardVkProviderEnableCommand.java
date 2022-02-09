package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.media.MediaActionService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.ConfigWizardVkProviderDescriptionService;
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

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderEnableCommand.class);

    private static final String NAME = "enable";

    private final DiscordProviderFromVkService vkProviderService;
    private final MediaActionService mediaActionService;
    private final ConfigWizardVkProviderDescriptionService descriptionService;

    @Autowired
    public ConfigWizardVkProviderEnableCommand(DiscordProviderFromVkService vkProviderService,
                                               MediaActionService mediaActionService,
                                               ConfigWizardVkProviderDescriptionService descriptionService) {
        this.vkProviderService = vkProviderService;
        this.mediaActionService = mediaActionService;
        this.descriptionService = descriptionService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromVk> providerOptional = vkProviderService.findById(entityId);
        if (providerOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get vkProvider={id={}}, ending privateSession={}", entityId, session);

            return ConfigWizardState.END;
        }
        DiscordProviderFromVk provider = providerOptional.get();

        if (provider.isEnabled()) {
            // todo already enabled response

            responses.add(descriptionService.getDescription(provider));

            return null;
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

        responses.add(descriptionService.getDescription(provider));

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
