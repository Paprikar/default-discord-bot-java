package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.dtofix;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.annotations.Required;
import com.vk.api.sdk.objects.callback.MessageNew;
import dev.paprikar.defaultdiscordbot.utils.MessageNewExclusionStrategy;

public class MessageNewFixed extends MessageNew {

    @SerializedName("object")
    @Required
    private MessageObjectFixed objectFixed;

    public MessageObjectFixed getObjectFixed() {
        return objectFixed;
    }

    public MessageNewFixed setObjectFixed(MessageObjectFixed objectFixed) {
        this.objectFixed = objectFixed;
        return this;
    }

    @Override
    public String toString() {
        return MessageNewExclusionStrategy.GSON_INSTANCE.toJson(this);
    }
}
