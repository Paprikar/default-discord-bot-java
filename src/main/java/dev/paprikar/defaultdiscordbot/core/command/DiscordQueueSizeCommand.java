package dev.paprikar.defaultdiscordbot.core.command;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The command for getting the queue size for certain categories.
 */
@Component
public class DiscordQueueSizeCommand implements DiscordCommand {

    private static final String NAME = "qsize";

    private final DiscordCategoryService categoryService;
    private final DiscordMediaRequestService mediaRequestService;

    private final RequestErrorHandler executionErrorHandler;

    /**
     * Constructs the command.
     *
     * @param categoryService an instance of {@link DiscordCategoryService}
     * @param mediaRequestService an instance of {@link DiscordMediaRequestService}
     */
    @Autowired
    public DiscordQueueSizeCommand(DiscordCategoryService categoryService,
                                   DiscordMediaRequestService mediaRequestService) {
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;

        this.executionErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while sending the command response")
                .build();
    }

    @Override
    public void execute(@Nonnull GuildMessageReceivedEvent event, @Nonnull String argsString) {
        List<DiscordCategory> targetCategories;
        List<DiscordCategory> allCategories = categoryService.findAllByGuildDiscordId(event.getGuild().getIdLong());

        if (argsString.isEmpty()) {
            targetCategories = allCategories;
        } else {
            targetCategories = new ArrayList<>();
            Set<String> targetCategoryNames = new HashSet<>(List.of(argsString.split(" ")));
            for (DiscordCategory category : allCategories) {
                if (targetCategoryNames.contains(category.getName())) {
                    targetCategories.add(category);
                }
            }
        }

        if (targetCategories.isEmpty()) {
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        for (DiscordCategory category : targetCategories) {
            String name = category.getName();
            long size = mediaRequestService.countByCategoryId(category.getId());
            embedBuilder.appendDescription(String.format("`%s`: %d\n", name, size));
        }

        MessageEmbed embed = embedBuilder
                .setColor(Color.GRAY)
                .setTitle("Queue size of categories")
                .setTimestamp(Instant.now())
                .build();

        event.getChannel()
                .sendMessageEmbeds(embed)
                .reference(event.getMessage())
                .queue(null, executionErrorHandler);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
