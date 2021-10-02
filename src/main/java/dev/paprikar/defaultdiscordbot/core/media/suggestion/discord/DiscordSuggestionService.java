package dev.paprikar.defaultdiscordbot.core.media.suggestion.discord;

import dev.paprikar.defaultdiscordbot.core.media.suggestion.FileExtension;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordSuggestionService {

    private final Logger logger = LoggerFactory.getLogger(DiscordSuggestionService.class);

    // Map<SuggestionChannelId, CategoryId>
    private final Map<Long, Long> categories = new ConcurrentHashMap<>();

    private final DiscordCategoryService categoryService;

    public DiscordSuggestionService(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * category:
     * name                   - ignore
     * sendingChannelId       - ignore
     * approvalChannelId      - ignore
     * startTime              - restart (send time)
     * endTime                - restart (send time)
     * reserveDays            - restart (send time)
     * positiveApprovalEmoji  - ignore
     * negativeApprovalEmoji  - ignore
     * +enabled               - start
     * -enabled               - stop
     * <p>
     * providers - ignore
     */

    public void handle(@Nonnull GuildMessageReceivedEvent event) {
        Long categoryId = categories.get(event.getChannel().getIdLong());
        if (categoryId == null) {
            return;
        }
        event.getMessage().delete().queue();
        String link = event.getMessage().getContentRaw();
        if (link.isEmpty()) { // from attachments
            List<Message.Attachment> attachments = event.getMessage().getAttachments();
            if (attachments.isEmpty()) {
                // todo invalid input response
                return;
            }
            if (attachments.size() != 1) {
                // todo invalid input response
                return;
            }
            Message.Attachment attachment = attachments.get(0);
            if (!(attachment.isImage() || attachment.isVideo())) {
                // todo invalid input response
                return;
            }
            link = attachment.getUrl();
        } else { // from content
            if (link.matches(".*?(\\R|\\s).*")) {
                // todo invalid input response
                return;
            }
//            if (!isValidExtension(link)) { // todo fix validation ?
//                // todo invalid input response
//                return;
//            }
        }
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();
        // todo thrust list
        TextChannel approvalChannel = event.getJDA().getTextChannelById(category.getApprovalChannelId());
        if (approvalChannel == null) {
            // todo failure callback (to suggestion channel)
            return;
        }
        // todo add suggester info
        approvalChannel.sendMessage(link).queue(
                m -> {
                    m.addReaction(category.getPositiveApprovalEmoji().toString()).complete();
                    m.addReaction(category.getNegativeApprovalEmoji().toString()).complete();
                    // todo success callback (to suggestion channel)
                },
                t -> {
                    // todo failure callback (to suggestion channel)
                }
        );
    }

    public void addSuggestionChannel(Long channelId, Long categoryId) {
        categories.put(channelId, categoryId);
    }

    public void removeSuggestionChannel(Long channelId) {
        categories.remove(channelId);
    }

    public void updateSuggestionChannel(Long oldChannelId, Long newChannelId) {
        Long categoryId = categories.get(oldChannelId);
        categories.put(newChannelId, categoryId);
        categories.remove(oldChannelId);
    }

    private boolean isValidExtension(String fileName) {
        String extension = getFileExtension(fileName);
        return extension != null && FileExtension.EXTENSIONS.contains(extension.toLowerCase());
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.') + 1;
        return index == 0 || index == fileName.length() ? null : fileName.substring(index);
    }
}
