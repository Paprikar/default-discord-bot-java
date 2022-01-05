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
public class ConfigWizardVkProviderDisableCommand implements ConfigWizardVkProviderCommand {

    private static final String NAME = "disable";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderDisableCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    private final MediaActionService mediaActionService;

    @Autowired
    public ConfigWizardVkProviderDisableCommand(DiscordProviderFromVkService vkProviderService,
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

            logger.error("execute(): Unable to get discordProvider={id={}}, ending session", entityId);

            return ConfigWizardState.END;
        }
        DiscordProviderFromVk provider = providerOptional.get();

        List<MessageEmbed> responses = session.getResponses();

        if (!provider.isEnabled()) {
            // todo already disabled response

            responses.add(ConfigWizardVkProviderService.getStateEmbed(provider));

            return null;
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

        responses.add(ConfigWizardVkProviderService.getStateEmbed(provider));

        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
