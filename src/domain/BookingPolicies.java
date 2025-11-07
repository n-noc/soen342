package domain;

public class BookingPolicies {

    //Daytime window: 06:00–22:00
    private static final int DAY_START_MIN = 6 * 60;
    private static final int DAY_END_MIN = 22 * 60;

    //Max layover duration (in minutes)
    private static final int MAX_LAYOVER_DAY_MIN = 120;
    private static final int MAX_LAYOVER_NIGHT_MIN = 30;

    //max travel time for policy 2
    private static final int MAX_EXTRA_VS_DIRECT_MIN = 180;

    private BookingPolicies() {
    }

    public static boolean isLayoverAllowed(Route first, Route next) {
        if (first == null || next == null) {
            return false;
        }

        if (!TransferRules.isValidConnection(first, next)) {
            return false;
        }

        //layover duration between arrival of first and departure of next
        int gap = TimeUtil.diff(first.getArrivalTime(), next.getDepartureTime());

        //Time of day at arrival of the first leg (in minutes since midnight)
        int arrMinutes = TimeUtil.minutes(first.getArrivalTime());

        boolean isNight
                = (arrMinutes < DAY_START_MIN) //before 06:00
                || (arrMinutes >= DAY_END_MIN);  //at or after 22:00

        int maxAllowed = isNight ? MAX_LAYOVER_NIGHT_MIN : MAX_LAYOVER_DAY_MIN;

        return gap <= maxAllowed;
    }

    public static boolean isOkComparedToDirect(Itinerary it, Integer directDurationMinutes) {
        if (it == null) {
            return false;
        }

        if (directDurationMinutes == null || directDurationMinutes == Integer.MAX_VALUE) {
            return true;
        }

        int total = it.getTotalDurationMinutes();
        return total <= directDurationMinutes + MAX_EXTRA_VS_DIRECT_MIN;
    }
}
