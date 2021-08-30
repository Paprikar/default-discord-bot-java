package dev.paprikar.defaultdiscordbot.config;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class SuggestionModule {

    @NotBlank
    private String directory;

    private int channelId;

    @NotBlank
    //@Value("${positive:U+2705}")
    private String positive = "U+2705";

    @NotBlank
    //@Value("${negative:U+274E}")
    private String negative = "U+274E";

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    @Override
    public String toString() {
        return "SuggestionModule{" +
                "directory='" + directory + '\'' +
                ", channelId=" + channelId +
                ", positive='" + positive + '\'' +
                ", negative='" + negative + '\'' +
                '}';
    }
}
