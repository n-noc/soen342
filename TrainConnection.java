import java.time.*;
import java.util.Set;

public class TrainConnection {

    private String routeID;
    public String departureCity;
    public String arrivalCity;
    public LocalTime departureTime;
    public LocalTime arrivalTime;
    public TrainType traintype;
    public Set<DayOfWeek> daysOfOperation;
    public int firstClassRate;
    public int secondClassRate;
    public Duration tripDuration;


    public TrainConnection(String routeID, String departureCity, String arrivalCity, LocalTime departureTime, LocalTime arrivalTime, TrainType traintype, Set<DayOfWeek> daysOfOperation, int firstClassRate, int secondClassRate, LocalTime tripDuration) {
        this.routeID = routeID;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.traintype = traintype;
        this.daysOfOperation = daysOfOperation;
        this.firstClassRate = firstClassRate;
        this.secondClassRate = secondClassRate;
        //calculating trip duration for 2 scenarios whether trip is >24h or less
        if (arrivalTime.isBefore(departureTime)) { this.tripDuration = Duration.between(departureTime, arrivalTime.plusHours(24)); }
        else { this.tripDuration = Duration.between(departureTime, arrivalTime); }
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
    public Duration getTripDuration() {
        return tripDuration;
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

    public String toString() {
        return departureCity + "->" + arrivalCity + "(" + departureTime + "->" + arrivalTime + ", trip duration" + tripDuration + ", train type: " + traintype + ", DAYS" + daysOfOperation + ", First Class: " + firstClassRate + ", Second Class: " + secondClassRate;
    }
}
