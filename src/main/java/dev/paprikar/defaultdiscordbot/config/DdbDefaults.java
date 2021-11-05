package dev.paprikar.defaultdiscordbot.config;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nonnull;
import java.util.List;

@Validated
public class DdbDefaults {

    private String prefix = "!";

    private Character positiveApprovalEmoji = 0x2705; // ✅

    private Character negativeApprovalEmoji = 0x274E; // ❎

    public DdbDefaults() {
    }

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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Character getPositiveApprovalEmoji() {
        return positiveApprovalEmoji;
    }

    public void setPositiveApprovalEmoji(Character positiveApprovalEmoji) {
        this.positiveApprovalEmoji = positiveApprovalEmoji;
    }

    public Character getNegativeApprovalEmoji() {
        return negativeApprovalEmoji;
    }

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

    static class PropertyFieldName {

        public static String PREFIX = "defaults.prefix";

        public static String POSITIVE_APPROVAL_EMOJI = "defaults.positive-approval-emoji";

        public static String NEGATIVE_APPROVAL_EMOJI = "defaults.negative-approval-emoji";
    }
}
