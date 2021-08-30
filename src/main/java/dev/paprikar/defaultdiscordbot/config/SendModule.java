package dev.paprikar.defaultdiscordbot.config;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class SendModule {

    @NotBlank
    private String directory;

    private int channelId;

    @NotBlank
    private String start;

    @NotBlank
    private String end;

    private int reserveDays;

    private String archiveDirectory;

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

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getReserveDays() {
        return reserveDays;
    }

    public void setReserveDays(int reserveDays) {
        this.reserveDays = reserveDays;
    }

    public String getArchiveDirectory() {
        return archiveDirectory;
    }

    public void setArchiveDirectory(String archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
    }

    @Override
    public String toString() {
        return "SendModule{" +
                "directory='" + directory + '\'' +
                ", channelId=" + channelId +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", reserveDays=" + reserveDays +
                ", archiveDirectory='" + archiveDirectory + '\'' +
                '}';
    }
}
