package domain;
import java.time.*;
import java.util.Set;

public class TrainConnection {

    private final String routeID;
    public String departureCity;
    public String arrivalCity;
    public LocalTime departureTime;
    public LocalTime arrivalTime;
    public TrainType traintype;
    public Set<DayOfWeek> daysOfOperation;
    public int firstClassRate;
    public int secondClassRate;
    private final java.time.Duration tripDuration;
    public int arrivalDayOffset;


    public TrainConnection(String routeID, String departureCity, String arrivalCity, LocalTime departureTime, LocalTime arrivalTime, TrainType traintype, Set<DayOfWeek> daysOfOperation, int firstClassRate, int secondClassRate, int arrivalDayOffset) {
        this.routeID = routeID;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.traintype = traintype;
        this.daysOfOperation = daysOfOperation;
        this.firstClassRate = firstClassRate;
        this.secondClassRate = secondClassRate;
        this.arrivalDayOffset = arrivalDayOffset;

        java.time.Duration d = java.time.Duration.between(departureTime, arrivalTime);

        if (arrivalDayOffset > 0) {
            d = d.plusDays(arrivalDayOffset);
        }

        if (d.isNegative()) {
            d = d.plusDays(1);
        }

        this.tripDuration = d;
    }

    public String getRouteID() {
        return routeID;
    }
    public String getDepartureCity() {
        return departureCity;
    }
    public String getArrivalCity() {
        return arrivalCity;
    }
    public LocalTime getDepartureTime() {
        return departureTime;
    }
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
    public TrainType getTraintype() {
        return traintype;
    }
    public Set<DayOfWeek> getDaysOfOperation() {
        return daysOfOperation;
    }
    public int getFirstClassRate() {
        return firstClassRate;
    }
    public int getSecondClassRate() {
        return secondClassRate;
    }
    public int getArrivalDayOffset() {
        return arrivalDayOffset;
    }
    public java.time.Duration getTripDuration() {
        return tripDuration;
    }

    private static String formatDuration(java.time.Duration d) {
        long minutes = d.toMinutes();
        long h = minutes / 60;
        long m = minutes % 60;
        if (h > 0 && m > 0) return h + "h " + m + "m";
        if (h > 0) return h + "h";
        return m + "m";
    }

    @Override
    public String toString() {
        return departureCity + "->" + arrivalCity + " (" + departureTime + "->" + arrivalTime + (arrivalDayOffset > 0 ? " (+" + arrivalDayOffset + "d)" : "") +
                ", trip: " + formatDuration(tripDuration) +
                ", train type: " + traintype +
                ", DAYS" + daysOfOperation +
                ", First Class: " + firstClassRate +
                ", Second Class: " + secondClassRate + ")";}
}
