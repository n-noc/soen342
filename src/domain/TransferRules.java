package domain;

// utility class for transfer rules between connecting trains 
// used when building indirect itineraries

public final class TransferRules {

    // min transfer times in minutes between trains at the same station
    private static final int SAME_CITY_MIN = 15;

    // min transfer time if the trains are in different cities
    private static final int DIFFERENT_CITY_MIN = 60;

    // additional delay if changing between train types
    private static final int TRAIN_TYPE_CHANGE_PENALTY = 10;

    // prevent instantiation
    private TransferRules() {}

    // compute minimum transfer time between 2 routes
    public static int computeMinTransfer(Route first, Route next) {
        if (first == null || next == null) {
            return DIFFERENT_CITY_MIN;
        }

        int base;

        // if arriving and departing from the same city
        if (first.getArrivalCity().equalsIgnoreCase(next.getDepartureCity())) {
            base = SAME_CITY_MIN;
        } else {
            base = DIFFERENT_CITY_MIN;
        }

        // if train types differ
        if (!first.getTrainType().equalsIgnoreCase(next.getTrainType())) {
            base += TRAIN_TYPE_CHANGE_PENALTY;
        }

        return base;
    }

    // check whether 2 routes can connect based on arrival and departure times
    public static boolean isValidConnection(Route first, Route next) {
        if (first == null || next == null) {
            return false;
        }

        // must arrive before next departs
        if (first.getArrivalCity().equalsIgnoreCase(next.getDepartureCity())) {

            // min required transfer time
            int minTransfer = computeMinTransfer(first, next);

            // compare using the string times
            int arr = timeToMinutes(first.getArrivalTime());
            int dep = timeToMinutes(next.getDepartureTime());

            // handle overnight trips
            if (dep < arr) {
                dep += 24 * 60;
            }

            return (dep - arr) >= minTransfer;
        }

        return false;
    }

    // helper to convert HH:mm to minutes since midnight
    private static int timeToMinutes(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) {
            return 0;
        }

        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}