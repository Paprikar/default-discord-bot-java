package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.dtofix;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.callback.MessageObject;

public class MessageObjectFixed extends MessageObject {

    @SerializedName("message")
    private MessageFixed messageFixed;

    public MessageFixed getMessageFixed() {
        return messageFixed;
    }

    public MessageObjectFixed setMessageFixed(MessageFixed messageFixed) {
        this.messageFixed = messageFixed;
        return this;
    }
}
