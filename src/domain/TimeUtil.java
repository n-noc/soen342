package domain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// utility class for time operations
// used for calculating durations and handling overnight trips

public final class TimeUtil {

    // standard time format used throughout project
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // prevent instantiation
    private TimeUtil() {}

    // convert "HH:mm" string to LocalTime
    public static LocalTime parse(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return LocalTime.MIDNIGHT;
        return LocalTime.parse(hhmm, FORMATTER);
    }

    // convert LocalTime back to "HH:mm" string
    public static String format(LocalTime t) {
        if (t == null) return "--:--";
        return t.format(FORMATTER);
    }

    // compute duration in minutes between two times, handling overnight cases
    public static int minutesBetween(String dep, String arr) {
        LocalTime t1 = parse(dep);
        LocalTime t2 = parse(arr);
        long diff = ChronoUnit.MINUTES.between(t1, t2);
        if (diff < 0) diff += 24 * 60; // handle next-day arrivals
        return (int) diff;
    }

    // check if one time is before another, considering overnight wrap
    public static boolean isBefore(String t1, String t2) {
        return minutes(t1) < minutes(t2);
    }

    // convert "HH:mm" to minutes since midnight
    public static int minutes(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) return 0;
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    // difference in minutes (positive only)
    public static int diff(String t1, String t2) {
        int m1 = minutes(t1);
        int m2 = minutes(t2);
        int diff = m2 - m1;
        return diff >= 0 ? diff : diff + 24 * 60;
    }
}
