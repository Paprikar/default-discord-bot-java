package dev.paprikar.defaultdiscordbot.core.session;

import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Data structure for storing information of the value of validator processing response or error, if any.
 * Either the value or the error will always be {@code null}.
 *
 * @param <T> the type of value processed by the validator
 */
public class DiscordValidatorProcessingResponse<T> {

    private final T value;

    private final MessageEmbed error;

    /**
     * Constructs a response.
     *
     * @param value the value obtained after processing by the validator
     * @param error the validator processing error
     */
    public DiscordValidatorProcessingResponse(T value, MessageEmbed error) {
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
