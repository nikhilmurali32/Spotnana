package com.skypath.util;

import com.skypath.model.Airport;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeCalculationUtils {

    /**
     * Parses a local date-time string and an IANA timezone string into a ZonedDateTime object.
     * @param localDateTimeStr e.g., "2023-10-25T14:30:00"
     * @param timezoneStr e.g., "America/New_York"
     * @return ZonedDateTime
     */
    public static ZonedDateTime parseToZonedDateTime(String localDateTimeStr, String timezoneStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr);
        ZoneId zoneId = ZoneId.of(timezoneStr);
        return ZonedDateTime.of(localDateTime, zoneId);
    }

    /**
     * Calculates the exact duration in minutes between two ZonedDateTime objects.
     * (handling date-line crossings automatically)
     */
    public static long calculateDurationInMinutes(ZonedDateTime start, ZonedDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Validates a layover. 
     * Returns true only if the layover is between 45 minutes (domestic) or 90 minutes (international)
     * and the strict 6-hour maximum. 'Domestic' means both airports share the same country code.
     */
    public static boolean isValidLayover(ZonedDateTime arrivalTime, ZonedDateTime departureTime, Airport airport1, Airport airport2) {
        long layoverMinutes = calculateDurationInMinutes(arrivalTime, departureTime);

        if (layoverMinutes > 360) {
            return false;
        }

        boolean isDomestic = airport1.country().equalsIgnoreCase(airport2.country());

        if (isDomestic) {
            return layoverMinutes >= 45;
        } else {
            return layoverMinutes >= 90;
        }
    }
}
