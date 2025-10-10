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

    // return the first route for the pair, or null if none. 
    public Route getRoute(String departureCity, String arrivalCity) {
        List<Route> list = routesByKey.get(keyFor(departureCity, arrivalCity));
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    // return all routes for a given city pair. 
    public List<Route> getRoutes(String departureCity, String arrivalCity) {
        List<Route> list = routesByKey.get(keyFor(departureCity, arrivalCity));
        return (list == null) ? List.of() : Collections.unmodifiableList(list);
    }

    // get routes for the pair of cities
    public List<Route> getAllRoutes() {
        List<Route> out = new ArrayList<>();
        for (List<Route> list : routesByKey.values()) {
            out.addAll(list);
        }
        return out;
    }

    // departuring connections raw connections list
    public List<TrainConnection> getDeparturesFrom(String city) {
        if (city == null) return List.of();
        String key = city.toLowerCase(Locale.ROOT).trim();
        return byDeparture.getOrDefault(key, List.of());
    }

    // Routes that depart from a given city filters the flattened list
    
    public List<Route> getRoutesFrom(String city) {
        if (city == null) return List.of();
        String want = city.trim();

        List<Route> out = new ArrayList<>();
        // routesByKey holds List<Route> for each pair; scan all lists
        for (List<Route> list : routesByKey.values()) {
            for (Route r : list) {
                if (r.getDepartureCity() != null
                        && r.getDepartureCity().equalsIgnoreCase(want)) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    // Indirect connections 
   // Finds all possible paths between 'from' and 'to' with up to maxStops transfers
    public List<List<TrainConnection>> findCityChains(String from, String to, int maxStops) {
        List<List<TrainConnection>> results = new ArrayList<>();

        if (from == null || to == null || maxStops < 1) return results;

        // Normalize
        from = from.trim().toLowerCase(Locale.ROOT);
        to = to.trim().toLowerCase(Locale.ROOT);

        // BFS through the network
        Queue<List<TrainConnection>> queue = new LinkedList<>();

        // Start with all direct departures from the starting city
        for (TrainConnection conn : getDeparturesFrom(from)) {
            List<TrainConnection> initialPath = new ArrayList<>();
            initialPath.add(conn);
            queue.add(initialPath);
        }

        while (!queue.isEmpty()) {
            List<TrainConnection> path = queue.poll();
            TrainConnection last = path.get(path.size() - 1);

            String arrival = last.getArrivalCity().toLowerCase(Locale.ROOT);

            // If we reached the destination, store this path
            if (arrival.equals(to)) {
                results.add(new ArrayList<>(path));
                continue;
            }

            // Limit number of stops 
            if (path.size() > maxStops) continue;

            // Explore further connections from the current arrival city
            for (TrainConnection next : getDeparturesFrom(arrival)) {
                // Avoid cycles, don’t revisit cities already in the path
                boolean alreadyVisited = path.stream()
                    .anyMatch(tc -> tc.getDepartureCity().equalsIgnoreCase(next.getArrivalCity()));
                if (alreadyVisited) continue;

                // Build a new extended path
                List<TrainConnection> newPath = new ArrayList<>(path);
                newPath.add(next);
                queue.add(newPath);
            }
        }

        return results;
    }

    // Index builders

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
            routesByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(route); 
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