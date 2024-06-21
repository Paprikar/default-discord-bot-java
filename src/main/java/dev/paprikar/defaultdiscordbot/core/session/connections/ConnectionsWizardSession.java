package dev.paprikar.defaultdiscordbot.core.session.connections;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.List;

/**
 * The container that stores the data needed to maintain the connections sessions.
 */
public class ConnectionsWizardSession {

    private PrivateChannel channel;

    private Long userId;

    private Long guildId;

    private ConnectionsWizard service;

    private List<MessageEmbed> responses = new ArrayList<>();

    /**
     * Constructs the container.
     *
     * @param member the user of the session
     * @param service the state instance of {@link ConnectionsWizard} the session is in
     *
     * @throws RuntimeException in case of any errors of opening a private channel with the target user.
     * See {@link RestAction#complete()} for more details
     * @see RestAction#complete()
     */
    public ConnectionsWizardSession(@Nonnull Member member, @Nonnull ConnectionsWizard service)
            throws RuntimeException {
        this.channel = member.getUser().openPrivateChannel().complete();
        this.userId = member.getUser().getIdLong();
        this.guildId = member.getGuild().getIdLong();
        this.service = service;
    }

    /**
     * @return the private channel with the target user of the session
     */
    public PrivateChannel getChannel() {
        return channel;
    }

    /**
     * @param channel the private channel with the target user of the session
     */
    public void setChannel(PrivateChannel channel) {
        this.channel = channel;
    }

    /**
     * @return the discord user id for which the session is created
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the discord user id for which the session is created
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return the id of the guild with which the current session is associated
     */
    public Long getGuildId() {
        return guildId;
    }

    /**
     * @param guildId the id of the guild with which the current session will be associated
     */
    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    /**
     * @return the state instance the session is in
     */
    public ConnectionsWizard getService() {
        return service;
    }

    /**
     * @param service the state instance that the session will be in
     */
    public void setService(ConnectionsWizard service) {
        this.service = service;
    }

    /**
     * @return the {@link List} of session responses
     */
    public List<MessageEmbed> getResponses() {
        return responses;
    }

    /**
     * @param responses the {@link List} of session responses
     */
    public void setResponses(List<MessageEmbed> responses) {
        this.responses = responses;
    }
}
