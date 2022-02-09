package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
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
public class ConfigWizardDiscordProviderRemoveCommand implements ConfigWizardDiscordProviderCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderRemoveCommand.class);

    private static final String NAME = "remove";

    private final DiscordProviderFromDiscordService discordProviderService;

    @Autowired
    public ConfigWizardDiscordProviderRemoveCommand(DiscordProviderFromDiscordService discordProviderService) {
        this.discordProviderService = discordProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     String argsString) {
        logger.trace("execute(): privateSession={}, argsString='{}'", session, argsString);

        Long entityId = session.getEntityId();
        List<MessageEmbed> responses = session.getResponses();

        Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService.findById(entityId);
        if (discordProviderOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get discordProvider={id={}, "
                    + "ending privateSession={}", entityId, session);

            return ConfigWizardState.END;
        }
        DiscordProviderFromDiscord provider = discordProviderOptional.get();

        if (provider.isEnabled()) {
            responses.add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The discord provider that is enabled cannot be deleted")
                    .build()
            );
            return null;
        }

        session.setEntityId(provider.getCategory().getId());
        discordProviderService.delete(provider);

        responses.add(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("The discord provider `" + provider.getName() + "` has been successfully deleted")
                .build()
        );

        logger.debug("The discordProvider={id={}} was deleted", provider.getId());

        return ConfigWizardState.DISCORD_PROVIDERS;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
