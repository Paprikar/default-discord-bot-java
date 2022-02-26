package dev.paprikar.defaultdiscordbot.config;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Container for storing default values.
 */
@Validated
public class DdbDefaults {

    private String prefix = "!";

    private Character positiveApprovalEmoji = 0x2705; // ✅

    private Character negativeApprovalEmoji = 0x274E; // ❎

    /**
     * Validates values.
     * <p>
     * The supplied {@link Errors errors} instance can be used to report any resulting validation errors.
     *
     * @param errors
     *         contextual state about the validation process
     *
     * @see ValidationUtils
     * @see org.springframework.validation.Validator#validate(Object, Errors) Validator.validate(Object, Errors)
     */
    void validate(@Nonnull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, PropertyFieldName.PREFIX, "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, PropertyFieldName.POSITIVE_APPROVAL_EMOJI, "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, PropertyFieldName.NEGATIVE_APPROVAL_EMOJI, "field.required");

        validateEmoji(positiveApprovalEmoji, PropertyFieldName.POSITIVE_APPROVAL_EMOJI, errors);
        validateEmoji(negativeApprovalEmoji, PropertyFieldName.NEGATIVE_APPROVAL_EMOJI, errors);
    }

    private void validateEmoji(Character value, String fieldName, Errors errors) {
        List<String> emojis = EmojiParser.extractEmojis(value.toString());

        if (emojis.isEmpty()) {
            errors.rejectValue(fieldName, "field.emoji.required", "The value must be an emoji");
        }
    }

    /**
     * @return the guild commands prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix
     *         the guild commands prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the category positive approval emoji
     */
    public Character getPositiveApprovalEmoji() {
        return positiveApprovalEmoji;
    }

    /**
     * @param positiveApprovalEmoji
     *         the category positive approval emoji
     */
    public void setPositiveApprovalEmoji(Character positiveApprovalEmoji) {
        this.positiveApprovalEmoji = positiveApprovalEmoji;
    }

    /**
     * @return the category negative approval emoji
     */
    public Character getNegativeApprovalEmoji() {
        return negativeApprovalEmoji;
    }

    /**
     * @param negativeApprovalEmoji
     *         the category negative approval emoji
     */
    public void setNegativeApprovalEmoji(Character negativeApprovalEmoji) {
        this.negativeApprovalEmoji = negativeApprovalEmoji;
    }

    @Override
    public String toString() {
        return "DdbDefaults{" +
                "prefix='" + prefix + '\'' +
                ", positiveApprovalEmoji=" + positiveApprovalEmoji +
                ", negativeApprovalEmoji=" + negativeApprovalEmoji +
                '}';
    }

    private static class PropertyFieldName {

        public static final String PREFIX = "defaults.prefix";

        public static final String POSITIVE_APPROVAL_EMOJI = "defaults.positive-approval-emoji";

        public static final String NEGATIVE_APPROVAL_EMOJI = "defaults.negative-approval-emoji";
    }
}
