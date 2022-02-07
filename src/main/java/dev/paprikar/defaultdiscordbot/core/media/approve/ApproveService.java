package dev.paprikar.defaultdiscordbot.core.media.approve;

import dev.paprikar.defaultdiscordbot.core.JDAService;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyKey;
import dev.paprikar.defaultdiscordbot.core.concurrency.ConcurrencyScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.MonitorService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.utils.JdaUtils.RequestErrorHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
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

@Service
public class ApproveService {

    private static final Logger logger = LoggerFactory.getLogger(ApproveService.class);

    private final DiscordCategoryService categoryService;
    private final DiscordMediaRequestService mediaRequestService;
    private final SendingService sendingService;
    private final MonitorService monitorService;

    // Map<ApprovalChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    // Map<CategoryId, ApprovalChannelId>
    private final Map<Long, Long> approvalChannels = new ConcurrentHashMap<>();

    private final RequestErrorHandler suggestionProcessingErrorHandler;

    @Autowired
    public ApproveService(DiscordCategoryService categoryService,
                          DiscordMediaRequestService mediaRequestService,
                          SendingService sendingService,
                          MonitorService monitorService) {
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;
        this.sendingService = sendingService;
        this.monitorService = monitorService;

        this.suggestionProcessingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while processing the suggestion")
                .build();
    }

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

                // todo suggester info
                if (emoji.equals(positiveEmoji)) {
                    message.delete().complete();
                    approve(category, message.getContentRaw());
                } else if (emoji.equals(negativeEmoji)) {
                    message.delete().complete();
                }
            } catch (RuntimeException e) {
                suggestionProcessingErrorHandler.accept(e);
            }
        }
    }

    public void submit(@Nonnull DiscordCategory category,
                       @Nonnull String url,
                       Consumer<Throwable> onSubmitError) {
        Long approvalChannelId = category.getApprovalChannelId();

        TextChannel approvalChannel = getTextChannel(approvalChannelId, onSubmitError);
        if (approvalChannel == null) {
            return;
        }

        String positiveEmoji = category.getPositiveApprovalEmoji().toString();
        String negativeEmoji = category.getNegativeApprovalEmoji().toString();

        logger.debug("submit(): Submitting the suggestion with url={} to text channel with id={}",
                url, approvalChannelId);

        Consumer<Void> onSubmitSuccess = unused -> logger.debug("submit(): The suggestion with url={} "
                + "was successfully submitted to text channel with id={}", url, approvalChannelId);

        // todo add suggester info
        approvalChannel.sendMessage(url)
                .flatMap(message -> message
                        .addReaction(positiveEmoji)
                        .and(message.addReaction(negativeEmoji)))
                .queue(onSubmitSuccess, onSubmitError);
    }

    public void approve(DiscordCategory category, String content) {
        mediaRequestService.save(new DiscordMediaRequest(category, content));
        sendingService.update(category);
    }

    public void add(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Long approvalChannelId = category.getApprovalChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Object monitor = new Object();

        synchronized (monitor) {
            if (monitorService.putIfAbsent(monitorKey, monitor) != null) {
                return;
            }

            categories.put(approvalChannelId, categoryId);
            approvalChannels.put(categoryId, approvalChannelId);
            monitorService.add(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        }
    }

    public void remove(@Nonnull DiscordCategory category) {
        Long categoryId = category.getId();
        Long approvalChannelId = category.getApprovalChannelId();

        ConcurrencyKey monitorKey = ConcurrencyKey.from(ConcurrencyScope.CATEGORY_APPROVE_CONFIGURATION, categoryId);
        Object monitor = monitorService.get(monitorKey);
        if (monitor == null) {
            return;
        }

        synchronized (monitor) {
            categories.remove(approvalChannelId);
            approvalChannels.remove(categoryId);
            monitorService.remove(monitorKey);
            monitorService.remove(ConcurrencyScope.CATEGORY_APPROVE, categoryId);
        }
    }

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
