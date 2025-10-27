package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// A complete trip plan composed of one or more legs.
public class Itinerary {

    //data
    private final List<Leg> legs = new ArrayList<>();

    // Derived totals 
    private int totalDurationMinutes;
    private int totalTransferMinutes;
    private int totalFirstClassPrice;
    private int totalSecondClassPrice;

    //constructors
    // Empty itinerary; 
    public Itinerary() {
    }

    /**
     * build a direct itinerary from a single route.
     */
    public static Itinerary fromSingleRoute(Route route) {
        Objects.requireNonNull(route, "route");
        Itinerary it = new Itinerary();
        it.addLeg(new Leg(route, 0, route.getDurationMinutes()));
        it.recomputeTotals();
        return it;
    }

    // ---- Mutators ----
    //Append a leg to the end of this itinerary.
    public void addLeg(Leg leg) {
        Objects.requireNonNull(leg, "leg");
        legs.add(leg);
    }

    // Recalculate all totals from legs.
    public void recomputeTotals() {
        int travel = 0;
        int transfers = 0;
        int first = 0;
        int second = 0;

        for (int i = 0; i < legs.size(); i++) {
            Leg L = legs.get(i);
            travel += L.getLegDurationMinutes();
            if (i > 0) {
                transfers += L.getTransferFromPrevMinutes();
            }

            Route r = L.getRoute();
            if (r != null) {
                first += r.getFirstClassPrice();
                second += r.getSecondClassPrice();
            }
        }

        this.totalTransferMinutes = transfers;
        this.totalDurationMinutes = travel + transfers;
        this.totalFirstClassPrice = first;
        this.totalSecondClassPrice = second;
    }

    //helpers
    // immutable view of legs
    public List<Leg> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    // treu if itinerary has exactly 1 leg (no transfers)
    public boolean isDirect() {
        return legs.size() == 1;
    }

    // Number of transfers = legs - 1 (never below 0)
    public int getTransferCount() {
        return Math.max(0, legs.size() - 1);
    }

    // first departure city, or null if empty
    public String getOriginCity() {
        if (legs.isEmpty()) {
            return null;
        }
        Route first = legs.get(0).getRoute();
        return first == null ? null : first.getDepartureCity();
    }

    // final arrival city, or null if empty. 
    public String getDestinationCity() {
        if (legs.isEmpty()) {
            return null;
        }
        Route last = legs.get(legs.size() - 1).getRoute();
        return last == null ? null : last.getArrivalCity();
    }

    // departure time of first leg ("HH:mm") or null. 
    public String getDepartureTime() {
        if (legs.isEmpty()) {
            return null;
        }
        Route first = legs.get(0).getRoute();
        return first == null ? null : first.getDepartureTime();
    }

    // arrival time of last leg ("HH:mm") or null.
    public String getArrivalTime() {
        if (legs.isEmpty()) {
            return null;
        }
        Route last = legs.get(legs.size() - 1).getRoute();
        return last == null ? null : last.getArrivalTime();
    }

    // totals
    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public int getTotalTransferMinutes() {
        return totalTransferMinutes;
    }

    public int getTotalFirstClassPrice() {
        return totalFirstClassPrice;
    }

    public int getTotalSecondClassPrice() {
        return totalSecondClassPrice;
    }

    // Two itineraries are considered equal if their ordered legs are equal.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Itinerary that)) {
            return false;
        }
        return legs.equals(that.legs);
    }

    @Override
    public int hashCode() {
        return legs.hashCode();
    }

    //good formating to print
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Itinerary{legs=").append(legs.size())
                .append(", origin=").append(getOriginCity())
                .append(", destination=").append(getDestinationCity())
                .append(", dep=").append(getDepartureTime())
                .append(", arr=").append(getArrivalTime())
                .append(", transfers=").append(getTransferCount())
                .append(", totalDuration=").append(totalDurationMinutes).append("m")
                .append(", xfer=").append(totalTransferMinutes).append("m")
                .append(", first€=").append(totalFirstClassPrice)
                .append(", second€=").append(totalSecondClassPrice)
                .append("}");

        // expand legs in a second line for debugging
        if (!legs.isEmpty()) {
            sb.append("\n  Legs:\n");
            for (int i = 0; i < legs.size(); i++) {
                Leg L = legs.get(i);
                Route r = L.getRoute();
                sb.append("   ")
                        .append(i + 1).append(") ")
                        .append(r.getDepartureCity()).append(" → ").append(r.getArrivalCity())
                        .append("  ").append(r.getDepartureTime()).append(" → ").append(r.getArrivalTime())
                        .append("  Train type ").append(r.getTrainType())
                        .append("  leg=").append(L.getLegDurationMinutes()).append("m")
                        .append(i == 0 ? "" : "  xfer=" + L.getTransferFromPrevMinutes() + "m")
                        .append("\n");
            }
        }
        return sb.toString();
    }
}
