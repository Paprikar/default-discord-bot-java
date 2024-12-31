package dev.paprikar.defaultdiscordbot.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.api.sdk.objects.callback.MessageNew;
import com.vk.api.sdk.objects.callback.MessageObject;
import com.vk.api.sdk.objects.messages.Message;

public class MessageNewExclusionStrategy implements ExclusionStrategy {

    public static final Gson GSON_INSTANCE = new GsonBuilder()
            .setExclusionStrategies(new MessageNewExclusionStrategy())
            .create();

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass().equals(MessageNew.class) && f.getName().equals("object") ||
                f.getDeclaringClass().equals(MessageObject.class) && f.getName().equals("message") ||
                f.getDeclaringClass().equals(Message.class) && f.getName().equals("attachments");
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
