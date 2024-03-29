package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.PhotoSizesType;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import com.vk.api.sdk.objects.wall.Wallpost;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.WallpostFull;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester.DiscordTrustedSuggesterService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnection.ProjectionDiscordUserId;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnectionService;
import dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.utils.JdaRequests.RequestErrorHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static dev.paprikar.defaultdiscordbot.utils.VkRequests.executeRequest;

/**
 * The service for handling vk suggestions.
 */
@Service
public class VkSuggestionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VkSuggestionHandler.class);

    private final DiscordCategoryService categoryService;
    private final DiscordTrustedSuggesterService trustedSuggesterService;
    private final DiscordUserVkConnectionService vkConnectionService;
    private final ApproveService approveService;
    private final SendingService sendingService;

    private final VkApiClient client = VkSuggestionService.CLIENT;

    private final Random random = new Random();

    private final Map<PhotoSizesType, Integer> photoSizeTypeIndexes = new EnumMap<>(PhotoSizesType.class);

    private final RequestErrorHandler suggestionSubmittingErrorHandler;

    /**
     * Constructs the service.
     *
     * @param categoryService
     *         an instance of {@link DiscordCategoryService}
     * @param trustedSuggesterService
     *         an instance of {@link DiscordTrustedSuggesterService}
     * @param vkConnectionService
     *         an instance of {@link DiscordUserVkConnectionService}
     * @param approveService
     *         an instance of {@link ApproveService}
     * @param sendingService
     *         an instance of {@link SendingService}
     */
    @Autowired
    public VkSuggestionHandler(DiscordCategoryService categoryService,
                               DiscordTrustedSuggesterService trustedSuggesterService,
                               DiscordUserVkConnectionService vkConnectionService,
                               ApproveService approveService,
                               SendingService sendingService) {
        this.categoryService = categoryService;
        this.trustedSuggesterService = trustedSuggesterService;
        this.vkConnectionService = vkConnectionService;
        this.approveService = approveService;
        this.sendingService = sendingService;

        // bigger value - bigger size
        // uncropped
        this.photoSizeTypeIndexes.put(PhotoSizesType.W, 10);
        this.photoSizeTypeIndexes.put(PhotoSizesType.Z, 9);
        this.photoSizeTypeIndexes.put(PhotoSizesType.Y, 8);
        this.photoSizeTypeIndexes.put(PhotoSizesType.X, 7);
        this.photoSizeTypeIndexes.put(PhotoSizesType.M, 6);
        this.photoSizeTypeIndexes.put(PhotoSizesType.S, 5);
        // cropped
        this.photoSizeTypeIndexes.put(PhotoSizesType.R, 4);
        this.photoSizeTypeIndexes.put(PhotoSizesType.Q, 3);
        this.photoSizeTypeIndexes.put(PhotoSizesType.P, 2);
        this.photoSizeTypeIndexes.put(PhotoSizesType.O, 1);

        this.suggestionSubmittingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while submitting the suggestion")
                .build();
    }

    /**
     * Handles new vk messages.
     *
     * @param message
     *         the {@link Message}
     * @param actor
     *         the {@link GroupActor}
     * @param provider
     *         the vk provider
     */
    public void handleMessageNewEvent(@Nonnull Message message,
                                      @Nonnull GroupActor actor,
                                      @Nonnull DiscordProviderFromVk provider) {
        List<String> urls = new ArrayList<>();

        for (MessageAttachment attachment : message.getAttachments()) {
            boolean pass = true;
            switch (attachment.getType()) {
                case WALL: {
                    pass = handleWallpostFullPhotos(attachment.getWall(), urls, message, actor);
                    break;
                }
                case PHOTO: {
                    pass = handlePhoto(attachment.getPhoto(), urls, message, actor);
                    break;
                }
            }

            if (!pass) {
                return;
            }
        }

        if (urls.isEmpty()) {
            executeRequest(client.messages().send(actor)
                    .randomId(random.nextInt())
                    .peerId(message.getPeerId())
                    .message("Error. The message must contain at least one image")
                    .replyTo(message.getId()));

            return;
        }

        Long categoryId = provider.getCategory().getId();
        Optional<DiscordCategory> categoryOptional = categoryService.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            return;
        }
        DiscordCategory category = categoryOptional.get();

        Integer vkUserId = message.getFromId();
        List<Long> discordUserIds = vkConnectionService.findAllByVkUserId(vkUserId).stream()
                .map(ProjectionDiscordUserId::getDiscordUserId)
                .collect(Collectors.toList());

        boolean isTrusted = trustedSuggesterService.existsByCategoryIdAndUserIdIn(categoryId, discordUserIds);
        boolean isBulkSubmit = category.isBulkSubmit();
        boolean isSendingSubmit = isTrusted && (urls.size() == 1 || isBulkSubmit);

        // todo transaction-like batch submit
        urls.forEach(url -> {
            if (isSendingSubmit) {
                sendingService.submit(category, url);
            } else {
                String userIdStr = message.getFromId().toString();

                List<GetResponse> responses = executeRequest(client.users().get(actor)
                        .userIds(userIdStr)
                        .fields(Fields.SCREEN_NAME));
                String userName = responses == null ? null : responses.get(0).getScreenName();

                MessageEmbed suggestion = new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTimestamp(Instant.now())
                        .appendDescription("Provider type: `VK`\n")
                        .appendDescription(String.format("Provider name: `%s`\n", provider.getName()))
                        .appendDescription(String.format("Author: [%s](https://vk.com/id%s)", userName, userIdStr))
                        .setImage(url)
                        .build();

                logger.debug("handleMessagePhotos(): Submitting the suggestion: "
                                + "provider={id={}}, userId={}, url={}, suggestion={timestamp={}}",
                        provider.getId(), userIdStr, url, suggestion.getTimestamp());

                approveService.submit(category, suggestion, suggestionSubmittingErrorHandler);
            }
        });

        executeRequest(client.messages().send(actor)
                .randomId(random.nextInt())
                .peerId(message.getPeerId())
                .message("Suggestion sent successfully")
                .replyTo(message.getId()));
    }

    private boolean handleWallpostFullPhotos(WallpostFull wallpostFull,
                                             List<String> urls,
                                             Message message,
                                             GroupActor actor) {
        List<Wallpost> copyHistory = wallpostFull.getCopyHistory();
        if (copyHistory != null) {
            for (Wallpost wallpost : copyHistory) {
                boolean pass = handleWallpostAttachmentsPhotos(wallpost.getAttachments(), urls, message, actor);
                if (!pass) {
                    return false;
                }
            }
        }

        return handleWallpostAttachmentsPhotos(wallpostFull.getAttachments(), urls, message, actor);
    }

    private boolean handleWallpostAttachmentsPhotos(List<WallpostAttachment> attachments,
                                                    List<String> urls,
                                                    Message message,
                                                    GroupActor actor) {
        if (attachments == null) {
            return true;
        }

        for (WallpostAttachment attachment : attachments) {
            if (attachment.getType() == WallpostAttachmentType.PHOTO) {
                boolean pass = handlePhoto(attachment.getPhoto(), urls, message, actor);
                if (!pass) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean handlePhoto(Photo photo, List<String> urls, Message message, GroupActor actor) {
        Optional<PhotoSizes> sizeOptional = photo.getSizes().stream()
                .max(Comparator.comparingInt(size -> photoSizeTypeIndexes.get(size.getType())));

        if (sizeOptional.isEmpty()) {
            logger.warn("handlePhotoAttachment(): Failed to get URL of the photo={}", photo);

            executeRequest(client.messages().send(actor)
                    .randomId(random.nextInt())
                    .peerId(message.getPeerId())
                    .message("Error. Failed to get URL of the photo")
                    .replyTo(message.getId()));

            return false;
        }

        String url = sizeOptional.get().getUrl().toString();
        urls.add(url);

        return true;
    }
}
