package dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordProviderFromDiscordService;
import dev.paprikar.defaultdiscordbot.core.session.PrivateSession;
import dev.paprikar.defaultdiscordbot.core.session.config.AbstractConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.command.ConfigWizardCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command.ConfigWizardDiscordProvidersAddCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command.ConfigWizardDiscordProvidersBackCommand;
import dev.paprikar.defaultdiscordbot.core.session.config.state.discordproviders.command.ConfigWizardDiscordProvidersOpenCommand;
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
public class ConfigWizardDiscordProvidersService extends AbstractConfigWizard {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardDiscordProvidersService.class);

    private final DiscordCategoryService categoryService;

    private final DiscordProviderFromDiscordService discordProviderService;

    private final ConfigWizardDiscordProvidersBackCommand backCommand;

    private final ConfigWizardDiscordProvidersAddCommand addCommand;

    private final ConfigWizardDiscordProvidersOpenCommand openCommand;

    @Autowired
    public ConfigWizardDiscordProvidersService(DiscordCategoryService categoryService,
                                               DiscordProviderFromDiscordService discordProviderService,
                                               ConfigWizardDiscordProvidersBackCommand backCommand,
                                               ConfigWizardDiscordProvidersAddCommand addCommand,
                                               ConfigWizardDiscordProvidersOpenCommand openCommand) {
        super();

        this.categoryService = categoryService;
        this.discordProviderService = discordProviderService;

        this.backCommand = backCommand;
        this.addCommand = addCommand;
        this.openCommand = openCommand;

        setupCommands();
    }

    public static MessageEmbed getStateEmbed(@Nonnull DiscordCategory category,
                                             @Nonnull List<DiscordProviderFromDiscord> providers) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "/discord providers`\n\n");

        if (!providers.isEmpty()) {
            builder.appendDescription("Discord providers:\n");
            for (DiscordProviderFromDiscord p : providers) {
                builder.appendDescription("`" + p.getName() + "`\n");
            }
            builder.appendDescription("\n");
        }

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`open` `<name>`\n");
        builder.appendDescription("`add` `<name>`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }

    @Nullable
    @Transactional
    @Override
    public ConfigWizardState handle(@Nonnull PrivateMessageReceivedEvent event, @Nonnull PrivateSession session) {
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
            Long categoryId = session.getEntityId();
            Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
            MessageEmbed embed;
            if (categoryOptional.isPresent()) {
                embed = getStateEmbed(categoryOptional.get(), discordProviderService.findAllByCategoryId(categoryId));
            } else {
                embed = null; // todo error response
                logger.error("print(): Unable to get category={id={}}", session.getEntityId());
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
        return ConfigWizardState.DISCORD_PROVIDERS;
    }

    private void setupCommands() {
        commands.put("back", backCommand);
        commands.put("add", addCommand);
        commands.put("open", openCommand);
    }
}
