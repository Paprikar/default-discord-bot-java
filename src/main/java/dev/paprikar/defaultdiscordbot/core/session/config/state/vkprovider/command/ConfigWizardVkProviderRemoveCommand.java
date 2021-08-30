package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardVkProviderRemoveCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderRemoveCommand.class);

    private final DiscordProviderFromVkService vkProviderService;

    public ConfigWizardVkProviderRemoveCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        logger.trace("onExitCommand(): event={}, sessionInfo={}, argsString='{}'", event, session, argsString);

        DiscordProviderFromVk provider = vkProviderService.getProviderById(session.getEntityId());
        if (provider.isEnabled()) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Vk provider that is enabled cannot be deleted")
                    .build()
            );
            return null;
        }
        session.setEntityId(provider.getCategory().getId());
        vkProviderService.deleteProvider(provider);
        session.getResponses().add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("Vk provider `" + provider.getName() + "` has been successfully deleted")
                .build()
        );

        logger.debug("The vkProvider={id={}} was deleted", provider.getId());
        return ConfigWizardState.VK_PROVIDERS;
    }
}
