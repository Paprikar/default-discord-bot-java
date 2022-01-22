package dev.paprikar.defaultdiscordbot.core.session;

import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizard;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.List;

public class PrivateSession {

    private RestAction<PrivateChannel> channel;

    private ConfigWizard service;

    private Long entityId;

    private Long discordGuildId;

    private List<MessageEmbed> responses = new ArrayList<>();

    public PrivateSession() {
    }

    public PrivateSession(RestAction<PrivateChannel> channel,
                          ConfigWizard service,
                          Long entityId,
                          Long discordGuildId) {
        this.channel = channel;
        this.service = service;
        this.entityId = entityId;
        this.discordGuildId = discordGuildId;
    }

    public RestAction<PrivateChannel> getChannel() {
        return channel;
    }

    public void setChannel(RestAction<PrivateChannel> channel) {
        this.channel = channel;
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

    public Long getDiscordGuildId() {
        return discordGuildId;
    }

    public void setDiscordGuildId(Long discordGuildId) {
        this.discordGuildId = discordGuildId;
    }

    public List<MessageEmbed> getResponses() {
        return responses;
    }

    public void setResponses(List<MessageEmbed> responses) {
        this.responses = responses;
    }
}
