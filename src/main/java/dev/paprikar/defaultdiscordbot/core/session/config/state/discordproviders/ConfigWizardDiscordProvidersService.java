package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command.ConfigWizardDiscordProvidersCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigWizardDiscordProvidersService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersService.class);

    private final DiscordCategoryService categoryService;
    private final DiscordProviderFromDiscordService discordProviderService;
    private final ConfigWizardDiscordProvidersDescriptionService descriptionService;

    @Autowired
    public ConfigWizardDiscordProvidersService(DiscordCategoryService categoryService,
                                               DiscordProviderFromDiscordService discordProviderService,
                                               ConfigWizardDiscordProvidersDescriptionService descriptionService,
                                               List<ConfigWizardDiscordProvidersCommand> commands) {
        super();

        this.categoryService = categoryService;
        this.discordProviderService = discordProviderService;
        this.descriptionService = descriptionService;

        commands.forEach(command -> this.commands.put(command.getName(), command));
    }

    @Nullable
    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull PrivateSession session) {
        logger.trace("handle(): privateSession={}", session);

        return super.handle(event, session);
    }

    @Transactional
    @Override
    public void print(@Nonnull PrivateSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Long categoryId = session.getEntityId();
            Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
            MessageEmbed embed;
            if (categoryOptional.isPresent()) {
                embed = descriptionService.getDescription(
                        categoryOptional.get(), discordProviderService.findAllByCategoryId(categoryId));
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get category={id={}}", session.getEntityId());
            }
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
        return ConfigWizardState.DISCORD_PROVIDERS;
    }
}
