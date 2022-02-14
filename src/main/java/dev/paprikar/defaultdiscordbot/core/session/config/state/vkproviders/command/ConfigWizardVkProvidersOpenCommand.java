package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
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

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        List<DiscordProviderFromVk> providers = vkProviderService.findAllByCategoryId(session.getEntityId());
        DiscordProviderFromVk targetProvider = providers.stream()
                .filter(provider -> argsString.equals(provider.getName()))
                .findFirst()
                .orElse(null);
        if (targetProvider == null) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The provider with the name `" + argsString + "` does not exist")
                    .build()
            );

            return ConfigWizardState.KEEP;
        }

        session.setEntityId(targetProvider.getId());

        logger.debug("Open at VK_PROVIDERS: privateSession={}, target='{}'", session, argsString);

        return ConfigWizardState.VK_PROVIDER;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
