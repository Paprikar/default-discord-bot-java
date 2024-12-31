package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.dtofix;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.messages.Message;

import java.util.List;

public class MessageFixed extends Message {

    @SerializedName("attachments")
    private List<MessageAttachmentFixed> attachmentsFixed;

    public List<MessageAttachmentFixed> getAttachmentsFixed() {
        return attachmentsFixed;
    }

    public MessageFixed setAttachmentsFixed(List<MessageAttachmentFixed> attachmentsFixed) {
        this.attachmentsFixed = attachmentsFixed;
        return this;
    }
}
