package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider;

import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command.ConfigWizardVkProviderCommand;
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
 * Service for the configuration state of vk provider directory.
 */
@Service
public class ConfigWizardVkProviderService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderService.class);

    private final DiscordProviderFromVkService vkProviderService;
    private final VkSuggestionService vkSuggestionService;

    /**
     * Constructs a configuration state service.
     *
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param vkSuggestionService
     *         an instance of {@link VkSuggestionService}
     * @param commands
     *         a {@link List} of instances of {@link ConfigWizardVkProviderCommand}
     */
    @Autowired
    public ConfigWizardVkProviderService(DiscordProviderFromVkService vkProviderService,
                                         VkSuggestionService vkSuggestionService,
                                         List<ConfigWizardVkProviderCommand> commands) {
        super();

        this.vkProviderService = vkProviderService;
        this.vkSuggestionService = vkSuggestionService;

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
            Optional<DiscordProviderFromVk> categoryOptional = vkProviderService.findById(providerId);
            if (categoryOptional.isEmpty()) {
                logger.error("print(): Unable to get vkProvider={id={}} for privateSession={}", providerId, session);
                return;
            }

            MessageEmbed embed = getDescription(categoryOptional.get());
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
        return ConfigWizardState.VK_PROVIDER;
    }

    private MessageEmbed getDescription(@Nonnull DiscordProviderFromVk provider) {
        DiscordCategory category = provider.getCategory();
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() +
                "/vk providers/" + provider.getName() + "`\n\n");

        String currentState = vkSuggestionService.contains(provider) ? "enabled" : "disabled";
        builder.appendDescription("Current state: `" + currentState + "`\n\n");

        String savedState = provider.isEnabled() ? "enabled" : "disabled";
        builder.appendDescription("Saved state: `" + savedState + "`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + provider.getName() + "`\n");
        builder.appendDescription("`groupId` = `" + provider.getGroupId() + "`\n");
        builder.appendDescription("`token` = `" + provider.getToken() + "`\n\n");

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`enable`\n");
        builder.appendDescription("`disable`\n");
        builder.appendDescription("`remove`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
