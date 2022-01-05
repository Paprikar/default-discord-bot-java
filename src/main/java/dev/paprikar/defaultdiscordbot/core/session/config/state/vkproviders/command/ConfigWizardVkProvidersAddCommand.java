package dev.paprikar.defaultdiscordbot.core.session.config.state.vkproviders.command;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
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
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@Component
public class ConfigWizardVkProvidersAddCommand implements ConfigWizardVkProvidersCommand {

    private static final String NAME = "add";

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProvidersAddCommand.class);

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromVkService vkProviderService;

    @Autowired
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
        // todo use name index ?
        for (DiscordProviderFromVk p : vkProviderService.findAllByCategoryId(categoryId)) {
            if (p.getName().equals(argsString)) {
                session.getResponses().add(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Configuration Wizard Error")
                        .setTimestamp(Instant.now())
                        .appendDescription("The name of vk provider must be unique")
                        .build()
                );
                return null;
            }
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            // todo error response

            logger.error("execute(): Unable to get category={id={}}, ending session", session.getEntityId());

            return ConfigWizardState.END;
        }

        DiscordProviderFromVk provider = new DiscordProviderFromVk();
        provider.setName(argsString);
        provider = vkProviderService.attach(provider, categoryOptional.get());

        session.setEntityId(provider.getId());

        logger.debug("Add at VK_PROVIDERS: name={}, session={}", argsString, session);

        return ConfigWizardState.VK_PROVIDER;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
}
