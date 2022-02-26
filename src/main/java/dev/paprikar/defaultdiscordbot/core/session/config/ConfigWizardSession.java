package dev.paprikar.defaultdiscordbot.core.session.config;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * The container that stores the data needed to maintain the configuration sessions.
 */
public class ConfigWizardSession {

    private PrivateChannel channel;

    private Long guildDiscordId;

    private ConfigWizard service;

    private Long entityId;

    private List<MessageEmbed> responses = new ArrayList<>();

    /**
     * Constructs the container.
     *
     * @param user
     *         the user of the session
     * @param guildDiscordId
     *         the guild discord id for which the session is created
     * @param service
     *         the state instance of {@link ConfigWizard} the session is in
     * @param entityId
     *         the id of the entity with which the current session state is associated
     *
     * @throws RuntimeException
     *         in case of any errors of opening a private channel with the target user.
     *         See {@link RestAction#complete()} for more details
     * @see RestAction#complete()
     */
    public ConfigWizardSession(@Nonnull User user,
                               @Nonnull Long guildDiscordId,
                               @Nonnull ConfigWizard service,
                               @Nonnull Long entityId)
            throws RuntimeException {
        this.channel = user.openPrivateChannel().complete();
        this.guildDiscordId = guildDiscordId;
        this.service = service;
        this.entityId = entityId;
    }

    /**
     * @return the private channel with the target user of the session
     */
    public PrivateChannel getChannel() {
        return channel;
    }

    /**
     * @param channel
     *         the private channel with the target user of the session
     */
    public void setChannel(PrivateChannel channel) {
        this.channel = channel;
    }

    /**
     * @return the guild discord id for which the session is created
     */
    public Long getGuildDiscordId() {
        return guildDiscordId;
    }

    /**
     * @param guildDiscordId
     *         the guild discord id for which the session is created
     */
    public void setGuildDiscordId(Long guildDiscordId) {
        this.guildDiscordId = guildDiscordId;
    }

    /**
     * @return the state instance the session is in
     */
    public ConfigWizard getService() {
        return service;
    }

    /**
     * @param service
     *         the state instance that the session will be in
     */
    public void setService(ConfigWizard service) {
        this.service = service;
    }

    /**
     * @return the id of the entity with which the current session state is associated
     */
    public Long getEntityId() {
        return entityId;
    }

    /**
     * @param entityId
     *         the id of the entity with which the current session state will be associated
     */
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    /**
     * @return the {@link List} of session responses
     */
    public List<MessageEmbed> getResponses() {
        return responses;
    }

    /**
     * @param responses
     *         the {@link List} of session responses
     */
    public void setResponses(List<MessageEmbed> responses) {
        this.responses = responses;
    }

    @Override
    public String toString() {
        return "ConfigWizardSession{" +
                "userId=" + channel.getUser().getId() +
                ", channelId=" + channel.getId() +
                ", guildDiscordId=" + guildDiscordId +
                ", state=" + service.getState() +
                ", entityId=" + entityId +
                '}';
    }
}
