package dev.paprikar.defaultdiscordbot.core.session.config.state.categories;

import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.categories.command.ConfigWizardCategoriesCommand;
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

@Service
public class ConfigWizardCategoriesService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoriesService.class);

    private final DiscordCategoryService categoryService;
    private final ConfigWizardCategoriesDescriptionService descriptionService;

    @Autowired
    public ConfigWizardCategoriesService(DiscordCategoryService categoryService,
                                         ConfigWizardCategoriesDescriptionService descriptionService,
                                         List<ConfigWizardCategoriesCommand> commands) {
        super();

        this.categoryService = categoryService;
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
            responses.add(descriptionService.getDescription(categoryService.findAllByGuildId(session.getEntityId())));
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
        return ConfigWizardState.CATEGORIES;
    }
}
