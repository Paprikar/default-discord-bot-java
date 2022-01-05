package dev.paprikar.defaultdiscordbot.core.media.suggestion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileExtensions {

    private static final List<String> imageExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "tiff", "svg", "apng"
    );

    public static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(imageExtensions);

    private static final List<String> videoExtensions = Arrays.asList(
            "webm", "flv", "vob", "avi", "mov", "wmv", "amv", "mp4", "mpg", "mpeg", "gifv"
    );

    public static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(videoExtensions);

    public static final Set<String> EXTENSIONS = new HashSet<>(imageExtensions.size() + videoExtensions.size());

    static {
        EXTENSIONS.addAll(imageExtensions);
        EXTENSIONS.addAll(videoExtensions);
    }
}
