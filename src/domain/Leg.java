package domain;

public class Leg {

    // represents one leg of a trip
    // one direct route between 2 cities

    private final Route route;
    private final int transferFromPrevMinutes;
    private final int legDurationMinutes;

    // constructors
    public Leg(Route route, int transferFromPrevMinutes, int legDurationMinutes){
        this.route=route;
        this.transferFromPrevMinutes=transferFromPrevMinutes;
        this.legDurationMinutes=legDurationMinutes;
    }

    // getters
    public Route getRoute() {
        return route;
    }

    public int getTransferFromPrevMinutes() {
        return transferFromPrevMinutes;
    }

    public int getLegDurationMinutes() {
        return legDurationMinutes;
    }

    // toString
    @Override
    public String toString() {
        return String.format("%s → %s (%s → %s, %d min, transfer %d min, type %s)",
                route.getDepartureCity(),
                route.getArrivalCity(),
                route.getDepartureTime(),
                route.getArrivalTime(),
                legDurationMinutes,
                transferFromPrevMinutes,
                route.getTrainType());
    }
    
}
