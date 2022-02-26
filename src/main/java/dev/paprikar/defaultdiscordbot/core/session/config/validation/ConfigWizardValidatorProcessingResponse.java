package dev.paprikar.defaultdiscordbot.core.session.config.validation;

import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nullable;

/**
 * Data structure for storing information of the value of validator processing response or error, if any.
 * Either the value or the error will always be {@code null}.
 *
 * @param <T>
 *         the type of value processed by the validator
 */
public class ConfigWizardValidatorProcessingResponse<T> {

    private final T value;

    private final MessageEmbed error;

    /**
     * Constructs a response.
     *
     * @param value
     *         the value obtained after processing by the validator
     * @param error
     *         the validator processing error
     */
    public ConfigWizardValidatorProcessingResponse(T value, MessageEmbed error) {
        this.value = value;
        this.error = error;
    }

    /**
     * @return the value obtained after processing by the validator, or {@code null} if error is provided
     */
    @Nullable
    public T getValue() {
        return value;
    }


    /**
     * @return the validator processing error, or {@code null} if value is provided
     */
    @Nullable
    public MessageEmbed getError() {
        return error;
    }
}
