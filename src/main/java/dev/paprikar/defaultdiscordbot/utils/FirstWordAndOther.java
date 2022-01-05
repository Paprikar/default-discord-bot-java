package dev.paprikar.defaultdiscordbot.utils;

import javax.annotation.Nonnull;

public class FirstWordAndOther {

    private final String firstWord;

    private final String other;

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

    @Nonnull
    public String getFirstWord() {
        return firstWord;
    }

    @Nonnull
    public String getOther() {
        return other;
    }
}
