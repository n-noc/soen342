import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;

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
            String key = keyFor(tc.getDepartureCity(), tc.getArrivalCity());
            //if key is already in map return it, otherwise create new one
            Route route = routesByKey.computeIfAbsent(
                    key, k -> new Route(tc.getDepartureCity(), tc.getArrivalCity()));
            route.addConnection(tc);
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
