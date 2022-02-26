package dev.paprikar.defaultdiscordbot.utils;

import javax.annotation.Nonnull;

/**
 * Splits the original string into two - with the first word (before the first space character)
 * and the rest of the string (after the first space character).
 */
public class FirstWordAndOther {

    private final String firstWord;

    private final String other;

    /**
     * Constructs a container with the result of string splitting.
     *
     * @param s
     *         the original string
     */
    public FirstWordAndOther(@Nonnull String s) {
        int index = s.indexOf(' ');
        if (index == -1) {
            firstWord = s;
            other = "";
        } else {
            firstWord = s.substring(0, index);
            other = s.substring(index + 1);
        }
    }

    /**
     * @return the first word (before the first space character),
     * or the original string if there is no space character.
     */
    public String getFirstWord() {
        return firstWord;
    }

    /**
     * @return the part of the original string that comes after the first
     * space character, or an empty string if there is no space character
     */
    public String getOther() {
        return other;
    }
}
