package dev.paprikar.defaultdiscordbot.core.media.suggestion.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class VkSuggestionValidator {

    @Nonnull
    public static List<MessageEmbed> validateInitially(@Nonnull DiscordProviderFromVk provider) {
        List<MessageEmbed> errors = new ArrayList<>();

        Integer groupId = provider.getGroupId();
        if (groupId == null) {
            // todo invalid param response
        }

        String token = provider.getToken();
        if (token == null) {
            // todo invalid param response
        }

        return errors;
    }

    @Nonnull
    public static List<MessageEmbed> validateFinally(@Nonnull DiscordProviderFromVk provider,
                                                     @Nonnull VkApiClient client) {
        List<MessageEmbed> errors = new ArrayList<>();

        GroupActor groupActor = new GroupActor(provider.getGroupId(), provider.getToken());
        try {
            client.groupsLongPoll().getLongPollServer(groupActor, provider.getGroupId()).execute();
        } catch (ApiException | ClientException e) {
            // todo invalid param response
        }

        return errors;
    }
}
