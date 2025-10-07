package infra;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;

import domain.Route;
import domain.TrainConnection;

public class TrainNetwork {

    private List<TrainConnection> allConnections = new ArrayList<>();

    
    private final Map<String, List<Route>> routesByKey = new HashMap<>();

    
    private final Map<String, List<TrainConnection>> byDeparture = new HashMap<>();

    public void load(String csvPath) throws IOException {
        this.allConnections = CsvLoader.load(csvPath);
        rebuildRoutesIndex();
        rebuildDepartureIndex();
    }

    public List<TrainConnection> getAllConnections() {
        return Collections.unmodifiableList(allConnections);
    }

    /** return the first route for the pair, or null if none. */
    public Route getRoute(String departureCity, String arrivalCity) {
        List<Route> list = routesByKey.get(keyFor(departureCity, arrivalCity));
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /** return all routes (trips) for a given city pair. */
    public List<Route> getRoutes(String departureCity, String arrivalCity) {
        List<Route> list = routesByKey.get(keyFor(departureCity, arrivalCity));
        return (list == null) ? List.of() : Collections.unmodifiableList(list);
    }

    /** Flatten all trips across all pairs. */
    public List<Route> getAllRoutes() {
        List<Route> out = new ArrayList<>();
        for (List<Route> list : routesByKey.values()) {
            out.addAll(list);
        }
        return out;
    }

    /** Existing: departuring connections (raw connections list). */
    public List<TrainConnection> getDeparturesFrom(String city) {
        if (city == null) return List.of();
        String key = city.toLowerCase(Locale.ROOT).trim();
        return byDeparture.getOrDefault(key, List.of());
    }

    /** Routes that depart from a given city (filters the flattened list). */
    public List<Route> getRoutesFrom(String city) {
        if (city == null) return List.of();
        String want = city.trim();
        List<Route> out = new ArrayList<>();
        for (List<Route> list : routesByKey.values()) {
            for (Route r : list) {
                if (r.getDepartureCity() != null && r.getDepartureCity().equalsIgnoreCase(want)) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    // Indirect connections 
    public List<List<TrainConnection>> findCityChains(String from, String to, int maxStops) {
        return List.of();
    }

    // ---------- Index builders ----------

    private void rebuildRoutesIndex() {
        routesByKey.clear();

        for (TrainConnection tc : allConnections) {
            // Convert Set<DayOfWeek> → "MTWTFSS"
            String daysStr;
            if (tc.getDaysOfOperation() != null) {
                StringBuilder sb = new StringBuilder(7);
                for (DayOfWeek d : DayOfWeek.values()) {
                    sb.append(tc.getDaysOfOperation().contains(d) ? d.name().charAt(0) : '-');
                }
                daysStr = sb.toString();
            } else {
                daysStr = "-------";
            }

            // Build a full Route from the connection
            Route route = new Route(
                    tc.getRouteID(),
                    tc.getDepartureCity(),
                    tc.getArrivalCity(),
                    tc.getDepartureTime().toString(),   // "HH:mm"
                    tc.getArrivalTime().toString(),     // "HH:mm"
                    tc.getTraintype().name(),           // enum → String
                    daysStr,
                    tc.getFirstClassRate(),
                    tc.getSecondClassRate()
            );

            String key = keyFor(tc.getDepartureCity(), tc.getArrivalCity());
            routesByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(route); // CHANGED: append, don’t overwrite
        }

        for (Map.Entry<String, List<Route>> e : routesByKey.entrySet()) {
            e.setValue(Collections.unmodifiableList(e.getValue()));
        }
    }

    private void rebuildDepartureIndex() {
        byDeparture.clear();
        for (TrainConnection tc : allConnections) {
            String key = tc.getDepartureCity() == null ? "" : tc.getDepartureCity().toLowerCase(Locale.ROOT).trim();
            byDeparture.computeIfAbsent(key, k -> new ArrayList<>()).add(tc);
        }
        for (Map.Entry<String, List<TrainConnection>> e : byDeparture.entrySet()) {
            e.setValue(Collections.unmodifiableList(e.getValue()));
        }
    }

    private static String keyFor(String from, String to) {
        String f = from == null ? "" : from.trim().toLowerCase(Locale.ROOT);
        String t = to   == null ? "" : to.trim().toLowerCase(Locale.ROOT);
        return f + "→" + t;
    }
}