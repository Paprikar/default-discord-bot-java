package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;

public class ConfigWizardVkProvidersAddCommand implements ConfigWizardCommand {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersAddCommand.class);

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromVkService vkProviderService;

    public ConfigWizardVkProvidersAddCommand(DiscordCategoryService categoryService,
                                             DiscordProviderFromVkService vkProviderService) {
        this.categoryService = categoryService;
        this.vkProviderService = vkProviderService;
    }

    @Nullable
    @Override
    public ConfigWizardState execute(@Nonnull PrivateMessageReceivedEvent event,
                                     @Nonnull PrivateSession session,
                                     @Nullable String argsString) {
        if (argsString == null) {
            logger.error("Required argument 'argsString' is missing");
            // todo internal error response
            return null;
        }
        if (argsString.isEmpty()) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The name cannot be empty")
                    .build()
            );
            return null;
        }
        if (argsString.length() > 32) {
            session.getResponses().add(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The length of the name cannot be more than 32 characters")
                    .build()
            );
            return null;
        }
        long categoryId = session.getEntityId();
        for (DiscordProviderFromVk p : vkProviderService.findProvidersByCategoryId(categoryId)) {
            if (p.getName().equals(argsString)) {
                session.getResponses().add(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("Vk provider name must be unique")
                        .build()
                );
                return null;
            }
        }
        DiscordProviderFromVk provider = new DiscordProviderFromVk();
        provider.setName(argsString);
        DiscordCategory category = categoryService.getCategoryById(categoryId);
        provider = vkProviderService.attach(provider, category);

        session.setEntityId(provider.getId());

        logger.debug("Add at VK_PROVIDERS: name={}, session={}", argsString, session);
        return ConfigWizardState.VK_PROVIDER;
    }
}
