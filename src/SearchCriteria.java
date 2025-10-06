import java.time.*;

import domain.TrainType;
public class SearchCriteria {

    private String departureCity, arrivalCity;
    private DayOfWeek day;
    private LocalTime earliestDeparture, latestDeparture;
    private LocalTime earliestArrival, latestArrival;
    private TrainType trainType;
    private Integer maxFirstClass, maxSecondClass;
    private Duration maxDuration;

    public SearchCriteria() {

    }

    //getters
    public String getDepartureCity() { return departureCity; }

    public String getArrivalCity() { return arrivalCity; }

    public DayOfWeek getDay() { return day; }

    public LocalTime getEarliestDeparture() { return earliestDeparture; }

    public LocalTime getLatestDeparture() { return latestDeparture; }

    public LocalTime getEarliestArrival() { return earliestArrival; }

    public LocalTime getLatestArrival() { return latestArrival; }

    public TrainType getTrainType() { return trainType; }

    public Integer getMaxFirstClass() { return maxFirstClass; }

    public Integer getMaxSecondClass() { return maxSecondClass; }

    public Duration getMaxDuration() { return maxDuration; }

    //setters
    public void setDepartureCity(String departureCity) { this.departureCity = safeTrim(departureCity); }

    public void setArrivalCity(String arrivalCity) { this.arrivalCity = safeTrim(arrivalCity); }

    public void setDay(DayOfWeek day) { this.day = day; }

    public void setEarliestDeparture(LocalTime earliestDeparture) { this.earliestDeparture = earliestDeparture; }

    public void setLatestDeparture(LocalTime latestDeparture) { this.latestDeparture = latestDeparture; }

    public void setEarliestArrival(LocalTime earliestArrival) { this.earliestArrival = earliestArrival; }

    public void setLatestArrival(LocalTime latestArrival) { this.latestArrival = latestArrival; }

    public void setTrainType(TrainType trainType) { this.trainType = trainType; }

    public void setMaxFirstClass(Integer maxFirstClass) { this.maxFirstClass = maxFirstClass; }

    public void setMaxSecondClass(Integer maxSecondClass) { this.maxSecondClass = maxSecondClass; }

    public void setMaxDuration(Duration maxDuration) { this.maxDuration = maxDuration; }

    //empty if user set no filters
    public boolean isEmpty() {
        return departureCity == null
                && arrivalCity == null
                && day == null
                && earliestDeparture == null
                && latestDeparture == null
                && earliestArrival == null
                && latestArrival == null
                && trainType == null
                && maxFirstClass == null
                && maxSecondClass == null
                && maxDuration == null;
    }

    public void fixTimeWindows() {
        if (earliestDeparture != null && latestDeparture != null
                && earliestDeparture.isAfter(latestDeparture)) {
            LocalTime tmp = earliestDeparture;
            earliestDeparture = latestDeparture;
            latestDeparture = tmp;
        }
        if (earliestArrival != null && latestArrival != null
                && earliestArrival.isAfter(latestArrival)) {
            LocalTime tmp = earliestArrival;
            earliestArrival = latestArrival;
            latestArrival = tmp;
        }
    }

    //avoid whitespace issues
    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
