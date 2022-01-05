package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.ApiRequest;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.PhotoSizesType;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.WallpostFull;
import dev.paprikar.defaultdiscordbot.core.concurrency.LockService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

public class GroupLongPollApiHandler extends GroupLongPollApi {

    private static final Logger logger = LoggerFactory.getLogger(GroupLongPollApiHandler.class);

    private static final Random random = new Random();

    // bigger - first
    private static final List<PhotoSizesType> photoSizeTypes = List.of(
            // uncropped
            PhotoSizesType.W,
            PhotoSizesType.Z,
            PhotoSizesType.Y,
            PhotoSizesType.X,
            PhotoSizesType.M,
            PhotoSizesType.S,

            // cropped
            PhotoSizesType.R,
            PhotoSizesType.Q,
            PhotoSizesType.P,
            PhotoSizesType.O
    );

    private static final Map<PhotoSizesType, Integer> photoSizeTypeIndexes = new EnumMap<>(PhotoSizesType.class);

    static {
        IntStream
                .range(0, photoSizeTypes.size())
                .forEach(i -> photoSizeTypeIndexes.put(photoSizeTypes.get(i), i));
    }

    private final ApproveService approveService;

    public GroupLongPollApiHandler(VkApiClient client, GroupActor actor, int maxReconnectDelay,
                                   VkSuggestionService suggestionService, ApproveService approveService,
                                   LockService lockService) {
        super(client, actor, maxReconnectDelay, suggestionService, lockService);

        this.approveService = approveService;
    }

    public GroupLongPollApiHandler(VkApiClient client, GroupActor actor, int maxReconnectDelay, int waitTime,
                                   VkSuggestionService suggestionService, ApproveService approveService,
                                   LockService lockService) {
        super(client, actor, maxReconnectDelay, waitTime, suggestionService, lockService);

        this.approveService = approveService;
    }

    private static void onSuggestionSubmitSuccess() {
        logger.debug("onSuggestionSubmitSuccess(): The suggestion was successfully submitted");
    }

    private static void onSuggestionSubmitFailure(Throwable throwable) {
        logger.warn("onSuggestionSubmitFailure(): An error occurred while submitting the suggestion", throwable);
    }

    private static <T> T executeRequest(ApiRequest<T> request) {
        try {
            return request.execute();
        } catch (ClientException | ApiException e) {
            logger.warn("executeRequest(): An error occurred while executing the request", e);
            return null;
        }
    }

    @Override
    protected void messageNew(Integer groupId, Message message) {
        logger.debug("messageNew(): groupId={}, message={}", groupId, message);

        executeRequest(client.messages().markAsRead(actor)
                .peerId(message.getPeerId()));

        handleMessagePhotos(message);
    }

    private void handleMessagePhotos(Message message) {
        List<String> urls = new ArrayList<>();

        for (MessageAttachment attachment : message.getAttachments()) {
            boolean pass = true;
            switch (attachment.getType()) {
                case WALL: {
                    pass = handleWallpostPhotos(message, urls, attachment.getWall());
                    break;
                }
                case PHOTO: {
                    pass = handlePhoto(message, urls, attachment.getPhoto());
                    break;
                }
            }

            if (!pass) {
                break;
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
            approveService.submit(getProviderCached().getCategory(), url, (Runnable) null,
                    GroupLongPollApiHandler::onSuggestionSubmitSuccess,
                    GroupLongPollApiHandler::onSuggestionSubmitFailure);
        }

        executeRequest(client.messages().send(actor)
                .randomId(random.nextInt())
                .peerId(message.getPeerId())
                .message("Suggestion sent successfully")
                .replyTo(message.getId()));
    }

    private boolean handleWallpostPhotos(Message message, List<String> urls, WallpostFull wallpost) {
        for (WallpostAttachment attachment : wallpost.getAttachments()) {
            if (attachment.getType() == WallpostAttachmentType.PHOTO) {
                boolean pass = handlePhoto(message, urls, attachment.getPhoto());
                if (!pass) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean handlePhoto(Message message, List<String> urls, Photo photo) {
        Optional<PhotoSizes> sizeOptional = photo.getSizes().stream()
                .min(Comparator.comparingInt(size -> photoSizeTypeIndexes.get(size.getType())));

        if (sizeOptional.isEmpty()) {
            logger.warn("handlePhotoAttachment(): Failed to get URL of the photo={}", photo);

            executeRequest(client.messages().send(actor)
                    .randomId(random.nextInt())
                    .peerId(message.getPeerId())
                    .message("Error. Failed to get URL of the photo")
                    .replyTo(message.getId()));

            return false;
        }

        urls.add(sizeOptional.get().getUrl().toString());
        return true;
    }
}
