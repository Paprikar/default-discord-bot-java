package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider.command.ConfigWizardDiscordProviderCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigWizardDiscordProviderService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderService.class);

    private final DiscordProviderFromDiscordService discordProviderService;

    @Autowired
    public ConfigWizardDiscordProviderService(DiscordProviderFromDiscordService discordProviderService,
                                              List<ConfigWizardDiscordProviderCommand> commands) {
        super();

        this.discordProviderService = discordProviderService;

        for (ConfigWizardDiscordProviderCommand c : commands) {
            this.commands.put(c.getName(), c);
        }
    }

    public static MessageEmbed getStateEmbed(DiscordProviderFromDiscord provider) {
        DiscordCategory category = provider.getCategory();
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() +
                "/discord providers/" + provider.getName() + "`\n\n");

        String currentState = provider.isEnabled() && provider.getCategory().isEnabled() ? "enabled" : "disabled";
        builder.appendDescription("Current state: `" + currentState + "`\n\n");

        String savedState = provider.isEnabled() ? "enabled" : "disabled";
        builder.appendDescription("Saved state: `" + savedState + "`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + provider.getName() + "`\n");
        builder.appendDescription("`suggestionChannelId` = `" + provider.getSuggestionChannelId() + "`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`enable`\n");
        builder.appendDescription("`disable`\n");
        builder.appendDescription("`remove`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }

    @Nullable
    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull PrivateSession session) {
        logger.trace("handle(): event={}, sessionInfo={}", event, session);

        return super.handle(event, session);
    }

    @Transactional
    @Override
    public void print(@Nonnull PrivateSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService
                    .findById(session.getEntityId());
            MessageEmbed embed;
            if (discordProviderOptional.isPresent()) {
                embed = getStateEmbed(discordProviderOptional.get());
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get discordProvider={id={}}", session.getEntityId());
            }
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel().flatMap(channel -> channel.sendMessageEmbeds(responses)).queue();
            session.setResponses(new ArrayList<>());
        }
    }

    @Nonnull
    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.DISCORD_PROVIDER;
    }
}
