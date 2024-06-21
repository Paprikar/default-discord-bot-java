package dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggester;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSession;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardState;
import dev.paprikar.defaultdiscordbot.core.session.config.state.trustedsuggesters.command.ConfigWizardTrustedSuggestersCommand;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for the configuration state of trusted suggesters directory.
 */
@Service
public class ConfigWizardTrustedSuggestersService extends ConfigWizard {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWizardTrustedSuggestersService.class);

    private final DiscordCategoryService categoryService;
    private final DiscordTrustedSuggesterService trustedSuggesterService;

    /**
     * Constructs a configuration state service.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param trustedSuggesterService an instance of {@link DiscordTrustedSuggesterService}
     * @param commands a {@link List} of instances of {@link ConfigWizardTrustedSuggestersCommand}
     */
    @Autowired
    public ConfigWizardTrustedSuggestersService(DiscordCategoryService categoryService,
                                                DiscordTrustedSuggesterService trustedSuggesterService,
                                                List<ConfigWizardTrustedSuggestersCommand> commands) {
        super();

        this.categoryService = categoryService;
        this.trustedSuggesterService = trustedSuggesterService;

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
            DiscordCategory category = categoryOptional.get();

            MessageEmbed embed = getDescription(category, trustedSuggesterService.findAllByCategoryId(categoryId));
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
        return ConfigWizardState.TRUSTED_SUGGESTERS;
    }

    private MessageEmbed getDescription(DiscordCategory category, List<DiscordTrustedSuggester> suggesters) {
        EmbedBuilder builder = new EmbedBuilder();

        builder
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now());

        builder.appendDescription("Current directory: `/categories/" + category.getName() + "/trusted suggesters`\n\n");

        builder.appendDescription("Variables:\n");
        builder.appendDescription("`bulkSubmit` = `" + category.isBulkSubmit() + "`\n\n");

        if (!suggesters.isEmpty()) {
            builder.appendDescription("Trusted suggesters:\n");
            suggesters.stream()
                    .map(suggester -> {
                        Long userId = suggester.getUserId();
                        return User.fromId(userId).getAsMention() + " (id: `" + userId + "`)\n";
                    })
                    .forEach(builder::appendDescription);
            builder.appendDescription("\n");
        }

        builder.appendDescription("Available commands:\n");
        builder.appendDescription("`set` `<variable>` `<value>`\n");
        builder.appendDescription("`add` `<user id>`\n");
        builder.appendDescription("`remove` `<user id>`\n");
        builder.appendDescription("`back`\n");
        builder.appendDescription("`exit`");

        return builder.build();
    }
}
