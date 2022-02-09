package dev.paprikar.defaultdiscordbot.core.session;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PrivateSession {

    private PrivateChannel channel;

    private Long guildDiscordId;

    private ConfigWizard service;

    private Long entityId;

    private List<MessageEmbed> responses = new ArrayList<>();

    public PrivateSession(@Nonnull User user,
                          @Nonnull Long guildDiscordId,
                          @Nonnull ConfigWizard service,
                          @Nonnull Long entityId)
            throws RuntimeException {
        this.channel = user.openPrivateChannel().complete();
        this.guildDiscordId = guildDiscordId;
        this.service = service;
        this.entityId = entityId;
    }

    public PrivateChannel getChannel() {
        return channel;
    }

    public void setChannel(PrivateChannel channel) {
        this.channel = channel;
    }

    public Long getGuildDiscordId() {
        return guildDiscordId;
    }

    public void setGuildDiscordId(Long guildDiscordId) {
        this.guildDiscordId = guildDiscordId;
    }

    public ConfigWizard getService() {
        return service;
    }

    public void setService(ConfigWizard service) {
        this.service = service;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public List<MessageEmbed> getResponses() {
        return responses;
    }

    public void setResponses(List<MessageEmbed> responses) {
        this.responses = responses;
    }

    @Override
    public String toString() {
        return "PrivateSession{" +
                "userId=" + channel.getUser().getId() +
                ", channelId=" + channel.getId() +
                ", guildDiscordId=" + guildDiscordId +
                ", state=" + service.getState() +
                ", entityId=" + entityId +
                '}';
    }
}
