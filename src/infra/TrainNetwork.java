package infra;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;

import domain.Route;
import domain.TrainConnection;

public class TrainNetwork {

    private List<TrainConnection> allConnections = new ArrayList<>();
    //grouped in hash map by city pair
    private final Map<String, Route> routesByKey = new HashMap<>();
    //(help with indirect connections)
    private final Map<String, List<TrainConnection>> byDeparture = new HashMap<>();

    public void load(String csvPath) throws IOException {
        this.allConnections = CsvLoader.load(csvPath);
        rebuildRoutesIndex();
        rebuildDepartureIndex();
    }

    public List<TrainConnection> getAllConnections() {
        return Collections.unmodifiableList(allConnections);
    }

    public Route getRoute(String departureCity, String arrivalCity) {
        return routesByKey.get(keyFor(departureCity, arrivalCity));
    }

    public List<Route> getAllRoutes() {
        return new ArrayList<>(routesByKey.values());
    }

    public List<TrainConnection> getDeparturesFrom(String city) {
        if (city == null) return List.of();
        String key = city.toLowerCase(Locale.ROOT).trim();
        return byDeparture.getOrDefault(key, List.of());
    }

    //get all routes from one city by searching route map
    public List<Route> getRoutesFrom(String city) {
        if (city == null) return List.of();
        List<Route> out = new ArrayList<>();
        for (Route r : routesByKey.values()) {
            if (r.getDepartureCity().equalsIgnoreCase(city)) {
                out.add(r); // then place them in a new list and return it
            }
        }
        return out;
    }

    //Indirect connections
    public List<List<TrainConnection>> findCityChains(String from, String to, int maxStops) {
        return List.of();
    }

    private void rebuildRoutesIndex() {
        routesByKey.clear();
        for (TrainConnection tc : allConnections) {
            // Convert Set<DayOfWeek> → "MTWTFSS"
            String daysStr = "";
            if (tc.getDaysOfOperation() != null) {
                StringBuilder sb = new StringBuilder();
                for (DayOfWeek d : DayOfWeek.values()) {
                    if (tc.getDaysOfOperation().contains(d)) {
                        sb.append(d.name().charAt(0)); // e.g., MON -> M, TUE -> T
                    } else {
                        sb.append("-");
                    }
                }
                daysStr = sb.toString();
            }
    
            // Build a Route with all converted values
            Route route = new Route(
                tc.getRouteID(),                        // route ID
                tc.getDepartureCity(),                  // from
                tc.getArrivalCity(),                    // to
                tc.getDepartureTime().toString(),       // convert LocalTime -> "HH:mm"
                tc.getArrivalTime().toString(),         // convert LocalTime -> "HH:mm"
                tc.getTraintype().name(),               // TrainType enum -> String
                daysStr,                                // days of operation
                tc.getFirstClassRate(),                 // first class price
                tc.getSecondClassRate()                 // second class price
            );
    
            String key = keyFor(tc.getDepartureCity(), tc.getArrivalCity());
            routesByKey.put(key, route);
        }
    }

    private void rebuildDepartureIndex() {
        byDeparture.clear();
        for (TrainConnection tc : allConnections) {
            String key = tc.getDepartureCity() == null ? "" : tc.getDepartureCity().toLowerCase(Locale.ROOT).trim();
            byDeparture.computeIfAbsent(key, k -> new ArrayList<>()).add(tc);
        }
    }

    private static String keyFor(String from, String to) {
        String f = from == null ? "" : from.trim().toLowerCase(Locale.ROOT);
        String t = to   == null ? "" : to.trim().toLowerCase(Locale.ROOT);
        return f + "→" + t;
    }
}
