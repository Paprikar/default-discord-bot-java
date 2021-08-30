package dev.paprikar.defaultdiscordbot.core.session.config;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class ConfigWizardSetterResponse {

    private final boolean isPassed;

    private final MessageEmbed embed;

    public ConfigWizardSetterResponse(boolean isPassed, MessageEmbed embed) {
        this.isPassed = isPassed;
        this.embed = embed;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public MessageEmbed getEmbed() {
        return embed;
    }
}
