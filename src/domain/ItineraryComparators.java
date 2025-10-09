package domain;

import java.util.Comparator;

// utility class for comparing and sorting itineraries 
// used when displaying multiple options for indirect routes

public final class ItineraryComparators {

    // prevent instantiation
    private ItineraryComparators() {}

    // compare by total duration (ascending)
    public static final Comparator<Itinerary> BY_TOTAL_DURATION =
            Comparator.comparingInt(Itinerary::getTotalDurationMinutes);

    // compare by number of transfers (fewer first)
    public static final Comparator<Itinerary> BY_TRANSFERS =
            Comparator.comparingInt(it -> it.getLegs().size() - 1);

    // compare by total first-class price
    public static final Comparator<Itinerary> BY_FIRST_CLASS_PRICE =
            Comparator.comparingInt(Itinerary::getTotalFirstClassPrice);

    // compare by total second-class price
    public static final Comparator<Itinerary> BY_SECOND_CLASS_PRICE =
            Comparator.comparingInt(Itinerary::getTotalSecondClassPrice);

    // compare by arrival time of the final leg
    public static final Comparator<Itinerary> BY_ARRIVAL_TIME = Comparator.comparingInt(it -> {
        if (it.getLegs().isEmpty()) return Integer.MAX_VALUE;
        Leg last = it.getLegs().get(it.getLegs().size() - 1);
        return timeToMinutes(last.getRoute().getArrivalTime());
    });

    // helper to convert HH:mm to minutes since midnight
    private static int timeToMinutes(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) return 0;
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}