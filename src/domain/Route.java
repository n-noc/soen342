package domain;

import java.time.*;

public class Route {
    private String routeId;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;  // "HH:mm"
    private String arrivalTime;    // "HH:mm"
    private String trainType;
    private String daysOfOperation; // e.g. "MTWTFSS"
    private int firstClassPrice;
    private int secondClassPrice;

    public Route(String routeId, String departureCity, String arrivalCity,
                 String departureTime, String arrivalTime, String trainType,
                 String daysOfOperation, int firstClassPrice, int secondClassPrice) {
        this.routeId = routeId;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainType = trainType;
        this.daysOfOperation = daysOfOperation;
        this.firstClassPrice = firstClassPrice;
        this.secondClassPrice = secondClassPrice;
    }

    // -------- Getters --------
    public String getRouteId() { return routeId; }
    public String getDepartureCity() { return departureCity; }
    public String getArrivalCity() { return arrivalCity; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getTrainType() { return trainType; }
    public int getFirstClassPrice() { return firstClassPrice; }
    public int getSecondClassPrice() { return secondClassPrice; }

    // Converts MTWTFSS to a set like MON,TUE,WED...
    public java.util.Set<String> getDaysSet() {
        java.util.Set<String> result = new java.util.HashSet<>();
        char[] map = {'M','T','W','T','F','S','S'};
        String[] days = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
        for (int i=0; i<Math.min(daysOfOperation.length(),7); i++) {
            char c = daysOfOperation.charAt(i);
            if (c == map[i]) result.add(days[i]);
        }
        return result;
    }

    // Computes trip duration in minutes. Handles overnight trips. 
    public int getDurationMinutes() {
        LocalTime dep = LocalTime.parse(departureTime);
        LocalTime arr = LocalTime.parse(arrivalTime);
        int mins = (int) Duration.between(dep, arr).toMinutes();
        if (mins < 0) mins += 24 * 60;
        return mins;
    }
}
