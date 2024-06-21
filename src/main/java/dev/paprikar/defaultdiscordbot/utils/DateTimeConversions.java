package dev.paprikar.defaultdiscordbot.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Date and time conversion utilities.
 */
public class DateTimeConversions {

    /**
     * Converts the source time from the source zone id to the destination zone id.
     * <p>
     * The conversion uses the local UTC date (see {@link LocalDate#now()}) to supplement the source time.
     *
     * @param srcTime the source time
     * @param srcZone the source zone id
     * @param dstZone the destination zone id
     *
     * @return the converted time
     */
    public static LocalTime convertLocalTimeForZones(LocalTime srcTime, ZoneId srcZone, ZoneId dstZone) {
        return ZonedDateTime
                .of(LocalDate.now(ZoneOffset.UTC), srcTime, srcZone) // todo fix potential error of one day
                .withZoneSameInstant(dstZone)
                .toLocalTime();
    }
}
