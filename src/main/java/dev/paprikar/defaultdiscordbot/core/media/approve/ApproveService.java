package dev.paprikar.defaultdiscordbot.core.media.approve;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service for approval of category suggestions.
 */
@Service
public class ApproveService {

    private static final Logger logger = LoggerFactory.getLogger(ApproveService.class);

    private final DiscordCategoryService categoryService;
    private final SendingService sendingService;
    private final MonitorService monitorService;

    // Map<ApprovalChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    // Map<CategoryId, ApprovalChannelId>
    private final Map<Long, Long> approvalChannels = new ConcurrentHashMap<>();

    private final RequestErrorHandler suggestionProcessingErrorHandler;

    /**
     * Constructs an approval service.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param sendingService
     *         an instance of {@link SendingService}
     * @param monitorService
     *         an instance of {@link MonitorService}
     */
    @Autowired
    public ApproveService(DiscordCategoryService categoryService,
                          SendingService sendingService,
                          MonitorService monitorService) {
        this.categoryService = categoryService;
        this.sendingService = sendingService;
        this.monitorService = monitorService;

        this.suggestionProcessingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while processing the suggestion")
                .build();
    }

    /**
     * Handles events of type {@link TextChannelDeleteEvent}.
     *
     * @param event
     *         the event of type {@link TextChannelDeleteEvent} for handling
     */
    public void handleTextChannelDeleteEvent(@Nonnull TextChannelDeleteEvent event) {
        Long channelId = event.getChannel().getIdLong();
        Long categoryId = categories.get(channelId);
        if (categoryId == null) {
            return;
        }

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            categories.remove(channelId);
            approvalChannels.remove(categoryId);
            monitorService.remove(monitorKey);
            monitorService.remove(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        }

        logger.debug("handleTextChannelDeleteEvent(): Media approve for category={id={}} is disabled "
                + "due to the deletion of the required text channel with id={}", categoryId, channelId);
    }

    /**
     * Handles events of type {@link GuildMessageReactionAddEvent}.
     *
     * @param event
     *         the event of type {@link GuildMessageReactionAddEvent} for handling
     */
    public void handleGuildMessageReactionAddEvent(@Nonnull GuildMessageReactionAddEvent event) {
        MessageReaction.ReactionEmote emote = event.getReactionEmote();
        if (emote.isEmote()) {
            return;
        }
        String emoji = emote.getEmoji();

        Long categoryId = categories.get(event.getChannel().getIdLong());
        if (categoryId == null) {
            return;
        }

        Object monitor = monitorService.get(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        if (monitor == null) {
            return;
        }

        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();

        synchronized (monitor) {
            try {
                Message message = event.retrieveMessage().complete();

                String positiveEmoji = category.getPositiveApprovalEmoji().toString();
                String negativeEmoji = category.getNegativeApprovalEmoji().toString();

                if (emoji.equals(positiveEmoji)) {
                    message.delete().complete();

                    String url = Objects.requireNonNull(message.getEmbeds().get(0).getImage()).getUrl();
                    sendingService.submit(category, url);
                } else if (emoji.equals(negativeEmoji)) {
                    message.delete().complete();
                }
            } catch (RuntimeException e) {
                suggestionProcessingErrorHandler.accept(e);
            }
        }
    }

    /**
     * Submits suggestion for further approval.
     *
     * @param category
     *         the suggestion category
     * @param suggestion
     *         suggestion embed, which includes information about the suggestion and
     *         the suggester, allowing it to be tracked within a particular provider
     * @param onSubmitError
     *         the handler for errors during submission
     */
    public void submit(@Nonnull DiscordCategory category,
                       @Nonnull MessageEmbed suggestion,
                       Consumer<Throwable> onSubmitError) {
        Long approvalChannelId = category.getApprovalChannelId();

        TextChannel approvalChannel = getTextChannel(approvalChannelId, onSubmitError);
        if (approvalChannel == null) {
            return;
        }

        String positiveEmoji = category.getPositiveApprovalEmoji().toString();
        String negativeEmoji = category.getNegativeApprovalEmoji().toString();

        Consumer<Void> onSubmitSuccess = unused -> logger.debug("submit(): The suggestion={timestamp={}} "
                        + "was successfully submitted to text channel with id={}",
                suggestion.getTimestamp(), approvalChannelId);

        approvalChannel.sendMessageEmbeds(suggestion)
                .flatMap(message -> message
                        .addReaction(positiveEmoji)
                        .and(message.addReaction(negativeEmoji)))
                .queue(onSubmitSuccess, onSubmitError);
    }

    /**
     * Adds the category to approval processing context.
     *
     * @param category
     *         the category
     */
    public void add(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Long approvalChannelId = category.getApprovalChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Object monitor = new Object();

        synchronized (monitor) {
            if (monitorService.putIfAbsent(monitorKey, monitor) != null) {
                logger.debug("add(): Category is already added. Skipping");
                return;
            }

            categories.put(approvalChannelId, categoryId);
            approvalChannels.put(categoryId, approvalChannelId);
            monitorService.add(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        }
    }

    /**
     * Removes the category from approval processing context.
     *
     * @param category
     *         the category
     */
    public void remove(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Long approvalChannelId = category.getApprovalChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            logger.debug("remove(): Category is already removed. Skipping");
            return;
        }

        synchronized (monitor) {
            categories.remove(approvalChannelId);
            approvalChannels.remove(categoryId);
            monitorService.remove(monitorKey);
            monitorService.remove(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        }
    }

    /**
     * Updates the category in approval processing context.
     *
     * @param category
     *         the category
     */
    public void update(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();

        Object monitor = monitorService.get(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            Long newApprovalChannelId = category.getApprovalChannelId();
            Long oldApprovalChannelId = approvalChannels.put(categoryId, newApprovalChannelId);

            // update approval channel id
            if (!Objects.equals(newApprovalChannelId, oldApprovalChannelId)) {
                categories.remove(oldApprovalChannelId);
                categories.put(newApprovalChannelId, categoryId);
            }
        }
    }

    /**
     * Does the category exists in approval processing context?
     *
     * @param category
     *         the category
     *
     * @return {@code true} if the category exists in approval processing context
     */
    public boolean contains(@Nonnull DiscordCategory category) {
        return approvalChannels.containsKey(category.getId());
    }

    private TextChannel getTextChannel(Long channelId, Consumer<Throwable> onSuggestionSubmitFailure) {
        JDA jda = JDAService.get();
        if (jda == null) {
            logger.error("getTextChannel(): Failed to get jda");
            return null;
        }

        TextChannel textChannel = jda.getTextChannelById(channelId);
        if (textChannel == null) {
            if (onSuggestionSubmitFailure != null) {
                Throwable error = new RuntimeException(
                        "getTextChannel(): Failed to discover discord text channel with id=" + channelId);
                onSuggestionSubmitFailure.accept(error);
            }
        }
        return textChannel;
    }
}
