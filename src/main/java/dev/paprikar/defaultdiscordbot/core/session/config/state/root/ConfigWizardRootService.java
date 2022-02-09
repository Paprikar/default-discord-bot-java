package dev.paprikar.defaultdiscordbot.core.session.config.state.root;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordGuildService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.root.command.ConfigWizardRootCommand;
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
public class ConfigWizardRootService extends AbstractConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardRootService.class);

    private final DiscordGuildService guildService;
    private final ConfigWizardRootDescriptionService descriptionService;

    @Autowired
    public ConfigWizardRootService(DiscordGuildService guildService,
                                   ConfigWizardRootDescriptionService descriptionService,
                                   List<ConfigWizardRootCommand> commands) {
        super();

        this.guildService = guildService;
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
            Optional<DiscordGuild> guildOptional = guildService.findById(session.getEntityId());
            MessageEmbed embed;
            if (guildOptional.isPresent()) {
                embed = descriptionService.getDescription(guildOptional.get());
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get guild={id={}}", session.getEntityId());
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
        return ConfigWizardState.ROOT;
    }
}
