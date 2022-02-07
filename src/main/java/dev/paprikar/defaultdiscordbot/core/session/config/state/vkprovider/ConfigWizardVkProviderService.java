package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command.ConfigWizardVkProviderCommand;
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
public class ConfigWizardVkProviderService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderService.class);

    private final DiscordProviderFromVkService vkProviderService;
    private final ConfigWizardVkProviderDescriptionService descriptionService;

    @Autowired
    public ConfigWizardVkProviderService(DiscordProviderFromVkService vkProviderService,
                                         ConfigWizardVkProviderDescriptionService descriptionService,
                                         List<ConfigWizardVkProviderCommand> commands) {
        super();

        this.vkProviderService = vkProviderService;
        this.descriptionService = descriptionService;

        commands.forEach(command -> this.commands.put(command.getName(), command));
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
            Optional<DiscordProviderFromVk> categoryOptional = vkProviderService.findById(session.getEntityId());
            MessageEmbed embed;
            if (categoryOptional.isPresent()) {
                embed = descriptionService.getDescription(categoryOptional.get());
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get vkProvider={id={}}", session.getEntityId());
            }
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel()
                    .flatMap(channel -> channel.sendMessageEmbeds(responses))
                    .queue(null, printingErrorHandler);
            session.setResponses(new ArrayList<>());
        }
    }

    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.VK_PROVIDER;
    }
}
