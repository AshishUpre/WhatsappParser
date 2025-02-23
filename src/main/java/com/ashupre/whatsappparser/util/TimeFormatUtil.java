package com.ashupre.whatsappparser.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeFormatUtil {

    // convert to utc before storing in db
    public static Instant localToUTC(LocalDateTime localTimestamp, ZoneId zoneId) {
        return localTimestamp.atZone(zoneId).toInstant();
    }

    // convert to local after getting back
    public static LocalDateTime utcToLocal(Instant timestamp, ZoneId zoneId) {
        return timestamp.atZone(zoneId).toLocalDateTime();
    }
}
