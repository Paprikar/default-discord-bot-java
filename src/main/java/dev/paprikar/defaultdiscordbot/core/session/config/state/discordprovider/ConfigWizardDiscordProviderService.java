package dev.paprikar.defaultdiscordbot.core.session.config.state.discordprovider;

import dev.paprikar.defaultdiscordbot.core.media.suggestion.discord.DiscordSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
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
import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for the configuration state of discord provider directory.
 */
@Service
public class ConfigWizardDiscordProviderService extends ConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProviderService.class);

    private final DiscordProviderFromDiscordService discordProviderService;
    private final DiscordSuggestionService discordSuggestionService;

    /**
     * Constructs a configuration state service.
     *
     * @param discordProviderService
     *         an instance of {@link DiscordProviderFromDiscordService}
     * @param discordSuggestionService
     *         an instance of {@link DiscordSuggestionService}
     * @param commands
     *         a {@link List} of instances of {@link ConfigWizardDiscordProviderCommand}
     */
    @Autowired
    public ConfigWizardDiscordProviderService(DiscordProviderFromDiscordService discordProviderService,
                                              DiscordSuggestionService discordSuggestionService,
                                              List<ConfigWizardDiscordProviderCommand> commands) {
        super();

        this.discordProviderService = discordProviderService;
        this.discordSuggestionService = discordSuggestionService;

        commands.forEach(command -> this.commands.put(command.getName(), command));
    }

    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull ConfigWizardSession session) {
        logger.trace("handle(): privateSession={}", session);

        return super.handle(event, session);
    }

    @Transactional
    @Override
    public void print(@Nonnull ConfigWizardSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Long providerId = session.getEntityId();
            Optional<DiscordProviderFromDiscord> discordProviderOptional = discordProviderService.findById(providerId);
            if (discordProviderOptional.isEmpty()) {
                logger.error("print(): Unable to get discordProvider={id={}} for privateSession={}",
                        providerId, session);
                return;
            }
            DiscordProviderFromDiscord provider = discordProviderOptional.get();

            MessageEmbed embed = getDescription(provider);
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel()
                    .sendMessageEmbeds(responses)
                    .queue(null, printingErrorHandler);
            session.setResponses(new ArrayList<>());
        }
    }

    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.DISCORD_PROVIDER;
    }

    private MessageEmbed getDescription(DiscordProviderFromDiscord provider) {
        DiscordCategory category = provider.getCategory();
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() +
                "/discord providers/" + provider.getName() + "`\n\n");

        String currentState = discordSuggestionService.contains(provider) ? "enabled" : "disabled";
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
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
