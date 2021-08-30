package dev.paprikar.defaultdiscordbot.core.session;

import dev.paprikar.defaultdiscordbot.core.session.config.IConfigWizard;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.List;

public class PrivateSession {

    private List<MessageEmbed> responses = new ArrayList<>();

    private RestAction<PrivateChannel> channel;

    private IConfigWizard service;

    private Long entityId;

    public PrivateSession() {
    }

    public PrivateSession(RestAction<PrivateChannel> channel, IConfigWizard service, Long entityId) {
        this.channel = channel;
        this.service = service;
        this.entityId = entityId;
    }

    public RestAction<PrivateChannel> getChannel() {
        return channel;
    }

    public void setChannel(RestAction<PrivateChannel> channel) {
        this.channel = channel;
    }

    public List<MessageEmbed> getResponses() {
        return responses;
    }

    public void setResponses(List<MessageEmbed> responses) {
        this.responses = responses;
    }

    public IConfigWizard getService() {
        return service;
    }

    public void setService(IConfigWizard service) {
        this.service = service;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
}
