package app;

import domain.Route;
import domain.TrainConnection;
import infra.TrainNetwork;
import search.SearchQuery;
import search.SearchService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        String csvPath = "./resources/eu_rail_network.csv";

        TrainNetwork net = new TrainNetwork();
        try {
            net.load(csvPath);
        } catch (IOException e) {
            System.err.println("Failed to load CSV: " + e.getMessage());
            return;
        }

        System.out.println("=== Loaded Data ===");
        System.out.println("Total connections loaded: " + net.getAllConnections().size());
        System.out.println("Total direct routes (city pairs): " + net.getAllRoutes().size());

        // ---- Show a few routes ----
        System.out.println("\n=== Sample Routes ===");
        net.getAllRoutes().stream().limit(5).forEach(r -> {
            System.out.printf("  [%s] %s → %s  dep %s  arr %s  type %s  first %d  second %d  days %s  dur %d min%n",
                    r.getRouteId(),
                    r.getDepartureCity(), r.getArrivalCity(),
                    r.getDepartureTime(), r.getArrivalTime(),
                    r.getTrainType(),
                    r.getFirstClassPrice(), r.getSecondClassPrice(),
                    r.getDaysSet(), r.getDurationMinutes());
        });

        // ---- Show a few departures (TrainConnection) from a city ----
        String city = "Alicante";
        System.out.println("\n=== Departures from " + city + " ===");
        net.getDeparturesFrom(city).stream().limit(5).forEach(tc -> {
            System.out.printf("  %s → %s  %s → %s  type %s  first %d  second %d  days %s%n",
                    tc.getDepartureCity(), tc.getArrivalCity(),
                    tc.getDepartureTime(), tc.getArrivalTime(),
                    tc.getTraintype(), tc.getFirstClassRate(), tc.getSecondClassRate(),
                    tc.getDaysOfOperation());
        });

        // ---- Direct route lookup by city pair (if present) ----
        String from = "A Coruña";
        String to   = "Santander";
        Route route = net.getRoute(from, to);
        if (route != null) {
            System.out.println("\n=== Direct Route " + from + " → " + to + " ===");
            System.out.printf("  [%s] dep %s  arr %s  type %s  first %d  second %d  days %s  dur %d min%n",
                    route.getRouteId(),
                    route.getDepartureTime(), route.getArrivalTime(),
                    route.getTrainType(),
                    route.getFirstClassPrice(), route.getSecondClassPrice(),
                    route.getDaysSet(), route.getDurationMinutes());
        } else {
            System.out.println("\nNo direct route " + from + " → " + to);
        }

        // ---- Issue 2: run a sample search using your SearchService ----
        System.out.println("\n=== Search (Issue 2 demo) ===");
        SearchQuery q = new SearchQuery(
                "alicante",          // fromCity (case-insensitive)
                null,                // toCity
                "08:00", "12:00",    // departure window [08:00..12:00]
                null, null,          // arrival window (none)
                null,                // trainType (any)
                null,                // days (any)
                "ANY",               // price class
                null,                // max price
                "DURATION",          // sort by duration
                "ASC"                // ascending
        );
        q.normalize();
        q.validate();

        List<Route> results = SearchService.direct(net, q);
        System.out.println("Results: " + results.size());
        results.stream().limit(5).forEach(r -> {
            System.out.printf("  %s → %s  dep %s  arr %s  dur %d min  type %s  first %d  second %d%n",
                    r.getDepartureCity(), r.getArrivalCity(),
                    r.getDepartureTime(), r.getArrivalTime(),
                    r.getDurationMinutes(), r.getTrainType(),
                    r.getFirstClassPrice(), r.getSecondClassPrice());
        });

        System.out.println("\nDone.");
    }
}