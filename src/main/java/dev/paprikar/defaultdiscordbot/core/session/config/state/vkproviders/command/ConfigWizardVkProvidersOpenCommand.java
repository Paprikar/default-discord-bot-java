package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
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
import java.util.Objects;

/**
 * The command for switching to a vk provider.
 */
@Component
public class ConfigWizardVkProvidersOpenCommand implements ConfigWizardVkProvidersCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersOpenCommand.class);

    private static final String NAME = "open";

    private final DiscordProviderFromVkService vkProviderService;

    /**
     * Constructs the command.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     */
    @Autowired
    public ConfigWizardVkProvidersOpenCommand(DiscordProviderFromVkService vkProviderService) {
        this.vkProviderService = vkProviderService;
    }

    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull ConfigWizardSession session,
                                     String argsString) {
        List<DiscordProviderFromVk> providers = vkProviderService.findAllByCategoryId(session.getEntityId());
        DiscordProviderFromVk targetProvider = providers.stream()
                .filter(provider -> Objects.equals(provider.getName(), argsString))
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
