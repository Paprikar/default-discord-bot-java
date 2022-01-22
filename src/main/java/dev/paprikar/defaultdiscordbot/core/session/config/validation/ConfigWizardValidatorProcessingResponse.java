package dev.paprikar.defaultdiscordbot.core.session.config.validation;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class ConfigWizardValidatorProcessingResponse<T> {

    private final T value;

    private final MessageEmbed error;

    public ConfigWizardValidatorProcessingResponse(T value, MessageEmbed error) {
        this.value = value;
        this.error = error;
    }

    public T getValue() {
        return value;
    }

    public MessageEmbed getError() {
        return error;
    }
}
