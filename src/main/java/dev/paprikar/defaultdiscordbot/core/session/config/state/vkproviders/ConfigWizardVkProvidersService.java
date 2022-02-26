package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command.ConfigWizardVkProvidersCommand;
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
 * Service for the configuration state of vk providers directory.
 */
@Service
public class ConfigWizardVkProvidersService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersService.class);

    private final DiscordCategoryService categoryService;
    private final DiscordProviderFromVkService vkProviderService;

    /**
     * Constructs a configuration state service.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param vkProviderService
     *         an instance of {@link DiscordProviderFromVkService}
     * @param commands
     *         a {@link List} of instances of {@link ConfigWizardVkProvidersCommand}
     */
    @Autowired
    public ConfigWizardVkProvidersService(DiscordCategoryService categoryService,
                                          DiscordProviderFromVkService vkProviderService,
                                          List<ConfigWizardVkProvidersCommand> commands) {
        super();

        this.categoryService = categoryService;
        this.vkProviderService = vkProviderService;

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
            Long categoryId = session.getEntityId();
            Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
            if (categoryOptional.isEmpty()) {
                logger.error("print(): Unable to get category={id={}} for privateSession={}", categoryId, session);
                return;
            }

            MessageEmbed embed = getDescription(
                    categoryOptional.get(), vkProviderService.findAllByCategoryId(categoryId));
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
        return ConfigWizardState.VK_PROVIDERS;
    }

    private MessageEmbed getDescription(@Nonnull DiscordCategory category,
                                        @Nonnull List<DiscordProviderFromVk> providers) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "/vk providers`\n\n");

        if (!providers.isEmpty()) {
            builder.appendDescription("Vk providers:\n");
            providers.stream()
                    .map(provider -> "`" + provider.getName() + "`\n")
                    .forEach(builder::appendDescription);
            builder.appendDescription("\n");
        }

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`open` `<name>`\n");
        builder.appendDescription("`add` `<name>`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
