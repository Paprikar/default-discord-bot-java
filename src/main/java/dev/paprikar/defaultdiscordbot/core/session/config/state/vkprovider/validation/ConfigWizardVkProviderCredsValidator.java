package dev.paprikar.defaultdiscordbot.core.session.config.state.vkprovider.validation;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import dev.paprikar.defaultdiscordbot.core.media.suggestion.vk.VkSuggestionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;

/**
 * The vk provider credentials validator in a configuration session.
 */
@Component
public class ConfigWizardVkProviderCredsValidator {

    private final VkApiClient client = VkSuggestionService.CLIENT;

    /**
     * Performs testing of credentials.
     *
     * @param groupId
     *         the group id to be tested
     * @param token
     *         the token to be tested
     *
     * @return the validator testing response
     */
    @Nullable
    public MessageEmbed test(@Nonnull Integer groupId, @Nonnull String token) {
        try {
            client.groupsLongPoll().getLongPollServer(new GroupActor(groupId, token), groupId).execute();
        } catch (ApiException | ClientException e) {
            return new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("The specified community id `" + groupId + "` and token are incorrect")
                    .build();
        }

        return null;
    }
}
