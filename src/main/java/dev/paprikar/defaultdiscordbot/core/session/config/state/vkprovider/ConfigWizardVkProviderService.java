package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromVkService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.command.*;
import dev.paprikar.defaultdiscordbot.utils.FirstWordAndOther;
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
public class ConfigWizardVkProviderService extends AbstractConfigWizard {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardVkProviderService.class);

    private final DiscordProviderFromVkService vkProviderService;

    private final ConfigWizardVkProviderBackCommand backCommand;

    private final ConfigWizardVkProviderSetCommand setCommand;

    private final ConfigWizardVkProviderEnableCommand enableCommand;

    private final ConfigWizardVkProviderDisableCommand disableCommand;

    private final ConfigWizardVkProviderRemoveCommand removeCommand;

    @Autowired
    public ConfigWizardVkProviderService(DiscordProviderFromVkService vkProviderService,
                                         ConfigWizardVkProviderBackCommand backCommand,
                                         ConfigWizardVkProviderSetCommand setCommand,
                                         ConfigWizardVkProviderEnableCommand enableCommand,
                                         ConfigWizardVkProviderDisableCommand disableCommand,
                                         ConfigWizardVkProviderRemoveCommand removeCommand) {
        super();

        this.vkProviderService = vkProviderService;

        this.backCommand = backCommand;
        this.setCommand = setCommand;
        this.enableCommand = enableCommand;
        this.disableCommand = disableCommand;
        this.removeCommand = removeCommand;

        setupCommands();
    }

    public static MessageEmbed getStateEmbed(DiscordProviderFromVk provider) {
        DiscordCategory category = provider.getCategory();
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() +
                "/vk providers/" + provider.getName() + "`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`name` = `" + provider.getName() + "`\n");
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

    @Nullable
    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event,
                                    @Nonnull PrivateSession session) {
        logger.trace("handle(): event={}, sessionInfo={}", event, session);

        String message = event.getMessage().getContentRaw();
        FirstWordAndOther parts = new FirstWordAndOther(message);
        String commandName = parts.getFirstWord().toLowerCase();
        ConfigWizardCommand command = commands.get(commandName);

        if (command == null) {
            // todo illegal command response ?
            return null;
        }

        String argsString = parts.getOther();
        return command.execute(event, session, argsString);
    }

    @Transactional
    @Override
    public void print(@Nonnull PrivateSession session, boolean addStateEmbed) {
        List<MessageEmbed> responses = session.getResponses();

        if (addStateEmbed) {
            Optional<DiscordProviderFromVk> categoryOptional = vkProviderService.findById(session.getEntityId());
            MessageEmbed embed;
            if (categoryOptional.isPresent()) {
                embed = getStateEmbed(categoryOptional.get());
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get vkProvider={id={}}", session.getEntityId());
            }
            responses.add(embed);
        }

        if (!responses.isEmpty()) {
            session.getChannel().flatMap(c -> c.sendMessageEmbeds(responses)).queue();
            session.setResponses(new ArrayList<>());
        }
    }

    @Nonnull
    @Override
    public ConfigWizardState getState() {
        return ConfigWizardState.VK_PROVIDER;
    }

    private void setupCommands() {
        commands.put("back", backCommand);
        commands.put("set", setCommand);
        commands.put("enable", enableCommand);
        commands.put("disable", disableCommand);
        commands.put("remove", removeCommand);
    }
}
