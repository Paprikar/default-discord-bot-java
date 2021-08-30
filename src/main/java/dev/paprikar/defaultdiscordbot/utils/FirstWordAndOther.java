package dev.paprikar.defaultdiscordbot.utils;

public class FirstWordAndOther {

    private final String firstWord;

    private final String other;

    public FirstWordAndOther(String s) {
        int index = s.indexOf(' ');
        if (index == -1) {
            firstWord = s;
            other = "";
        } else {
            firstWord = s.substring(0, index);
            other = s.substring(index + 1);
        }
    }

    public String getFirstWord() {
        return firstWord;
    }

    public String getOther() {
        return other;
    }
}
