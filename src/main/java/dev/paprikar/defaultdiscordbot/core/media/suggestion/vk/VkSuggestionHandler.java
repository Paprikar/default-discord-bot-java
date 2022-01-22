package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.PhotoSizesType;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.WallpostFull;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;

import static dev.paprikar.defaultdiscordbot.utils.VkUtils.executeRequest;

@Service
public class VkSuggestionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VkSuggestionHandler.class);

    private final ApproveService approveService;

    private final VkApiClient client = VkSuggestionService.CLIENT;

    private final Random random = new Random();

    private final Map<PhotoSizesType, Integer> photoSizeTypeIndexes = new EnumMap<>(PhotoSizesType.class);

    public VkSuggestionHandler(ApproveService approveService) {
        this.approveService = approveService;

        // bigger value - bigger size
        // uncropped
        photoSizeTypeIndexes.put(PhotoSizesType.W, 10);
        photoSizeTypeIndexes.put(PhotoSizesType.Z, 9);
        photoSizeTypeIndexes.put(PhotoSizesType.Y, 8);
        photoSizeTypeIndexes.put(PhotoSizesType.X, 7);
        photoSizeTypeIndexes.put(PhotoSizesType.M, 6);
        photoSizeTypeIndexes.put(PhotoSizesType.S, 5);
        // cropped
        photoSizeTypeIndexes.put(PhotoSizesType.R, 4);
        photoSizeTypeIndexes.put(PhotoSizesType.Q, 3);
        photoSizeTypeIndexes.put(PhotoSizesType.P, 2);
        photoSizeTypeIndexes.put(PhotoSizesType.O, 1);
    }

    private static void onSuggestionSubmitSuccess() {
        logger.debug("onSuggestionSubmitSuccess(): The suggestion was successfully submitted");
    }

    private static void onSuggestionSubmitFailure(Throwable throwable) {
        logger.warn("onSuggestionSubmitFailure(): An error occurred while submitting the suggestion", throwable);
    }

    public void handleMessageNewEvent(@Nonnull Message message,
                                      @Nonnull GroupActor actor,
                                      @Nonnull DiscordProviderFromVk provider) {
        List<String> urls = new ArrayList<>();

        for (MessageAttachment attachment : message.getAttachments()) {
            boolean pass = true;
            switch (attachment.getType()) {
                case WALL: {
                    pass = handleWallpostPhotos(attachment.getWall(), urls, message, actor);
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

        // todo thrust list

        // todo transaction-like batch submit
        for (String url : urls) {
            logger.debug("handleMessagePhotos(): Submitting the suggestion with url={}", url);
            approveService.submit(provider.getCategory(), url, (Runnable) null,
                    VkSuggestionHandler::onSuggestionSubmitSuccess,
                    VkSuggestionHandler::onSuggestionSubmitFailure);
        }

        executeRequest(client.messages().send(actor)
                .randomId(random.nextInt())
                .peerId(message.getPeerId())
                .message("Suggestion sent successfully")
                .replyTo(message.getId()));
    }

    private boolean handleWallpostPhotos(WallpostFull wallpost,
                                         List<String> urls,
                                         Message message,
                                         GroupActor actor) {
        for (WallpostAttachment attachment : wallpost.getAttachments()) {
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
