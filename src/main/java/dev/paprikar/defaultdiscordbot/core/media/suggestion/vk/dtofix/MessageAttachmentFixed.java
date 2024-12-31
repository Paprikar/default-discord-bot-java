package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.dtofix;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;

public class MessageAttachmentFixed extends MessageAttachment {

    /**
     * bruh.
     */
    @SerializedName("wall")
    private WallpostFull wall;

    public WallpostFull getWall() {
        return wall;
    }

    public MessageAttachmentFixed setWall(WallpostFull wall) {
        this.wall = wall;
        return this;
    }
}
