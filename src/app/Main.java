package app;

import domain.Route;
import domain.TrainConnection;
import domain.Itinerary;
import domain.ItineraryComparators;
import infra.TrainNetwork;
import search.SearchQuery;
import search.SearchService;
import search.IndirectSearchService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

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

        // ===== Search (Issue 2 real demos) =====
        System.out.println("\n=== Search (Issue 2 demos) ===");

        // 1) A Coruña → Santander — wide window to include 12:50
        {
            SearchQuery q1 = new SearchQuery(
                    "a coruña", "santander",
                    "00:00", "23:59",
                    null, null,
                    null,
                    null,
                    "ANY",
                    null,
                    "DURATION",
                    "ASC"
            );
            q1.normalize(); q1.validate();

            List<Route> results1 = SearchService.direct(net, q1);
            System.out.println("\n[1] A Coruña → Santander (any time)");
            if (results1.isEmpty()) {
                System.out.println("  No matches.");
            } else {
                results1.stream().limit(10).forEach(r -> System.out.printf(
                        "  %s → %s  dep %s  arr %s  dur %d min  type %s  1st€%d  2nd€%d%n",
                        r.getDepartureCity(), r.getArrivalCity(),
                        r.getDepartureTime(), r.getArrivalTime(),
                        r.getDurationMinutes(), r.getTrainType(),
                        r.getFirstClassPrice(), r.getSecondClassPrice()
                ));
                System.out.println("  ... total: " + results1.size());
            }
        }

        // 2) Alicante departures — afternoon/evening window
        {
            System.out.println("\n=== Debug: All routes from Alicante (verify index) ===");
            int alicanteRouteCount = 0;
            for (Route r : net.getAllRoutes()) {
                if (r.getDepartureCity() != null && r.getDepartureCity().equalsIgnoreCase("Alicante")) {
                    alicanteRouteCount++;
                    System.out.printf("  [DBG] %s → %s  dep %s  arr %s  type %s%n",
                            r.getDepartureCity(), r.getArrivalCity(),
                            r.getDepartureTime(), r.getArrivalTime(),
                            r.getTrainType());
                }
            }
            System.out.println("Total Alicante departures found in index: " + alicanteRouteCount);

            SearchQuery q2 = new SearchQuery(
                    "alicante", null,
                    "12:00", "23:59",
                    null, null,
                    null,
                    null,
                    "ANY",
                    null,
                    "DURATION",
                    "ASC"
            );
            q2.normalize(); q2.validate();

            List<Route> results2 = SearchService.direct(net, q2);
            System.out.println("\n[2] From Alicante 12:00–23:59 (by duration)");
            if (results2.isEmpty()) {
                System.out.println("  No matches.");
            } else {
                results2.stream().limit(10).forEach(r -> System.out.printf(
                        "  %s → %s  dep %s  arr %s  dur %d min  type %s  1st€%d  2nd€%d%n",
                        r.getDepartureCity(), r.getArrivalCity(),
                        r.getDepartureTime(), r.getArrivalTime(),
                        r.getDurationMinutes(), r.getTrainType(),
                        r.getFirstClassPrice(), r.getSecondClassPrice()
                ));
                System.out.println("  ... total: " + results2.size());
            }
        }

        // 3) From Madrid — cap second-class ≤ €80, sort by second-class price
        {
            SearchQuery q3 = new SearchQuery(
                    "madrid", null,
                    null, null,
                    null, null,
                    null,
                    null,
                    "SECOND",
                    80,
                    "PRICE_SECOND",
                    "ASC"
            );
            q3.normalize(); q3.validate();

            List<Route> results3 = SearchService.direct(net, q3);
            System.out.println("\n[3] From Madrid, 2nd-class ≤ €80 (by 2nd-class price)");
            if (results3.isEmpty()) {
                System.out.println("  No matches.");
            } else {
                results3.stream().limit(10).forEach(r -> System.out.printf(
                        "  %s → %s  dep %s  arr %s  type %s  2nd€%d  dur %d min%n",
                        r.getDepartureCity(), r.getArrivalCity(),
                        r.getDepartureTime(), r.getArrivalTime(),
                        r.getTrainType(), r.getSecondClassPrice(), r.getDurationMinutes()
                ));
                System.out.println("  ... total: " + results3.size());
            }
        }

        System.out.println("\nDone with Issue 2 demos.");

        // =========================
        // ===== Issue 3 demo  =====
        // =========================
        System.out.println("\n=== Issue 3: Indirect itineraries (max 2 transfers) ===");

        SearchQuery qi = new SearchQuery(
                "a coruña", "santander",
                "00:00", "23:59",    // broad window; adjust if you want
                null, null,
                null,
                null,
                "ANY",
                null,
                "DURATION",
                "ASC"
        );
        qi.normalize(); qi.validate();

      // find itineraries allowing up to 2 transfers (i.e., up to 3 legs)
        List<Itinerary> allItins = IndirectSearchService.find(net, qi, /*maxTransfers=*/2, /*maxResults=*/50);

        // ✅ Keep only itineraries that have at least 1 transfer (≥ 2 legs)
        List<Itinerary> itineraries = allItins.stream()
                .filter(it -> it.getLegs().size() > 1)
                .sorted(ItineraryComparators.BY_TOTAL_DURATION)
                .toList();

        System.out.println("Found multi-leg itineraries (with transfers): " + itineraries.size());

        if (itineraries.isEmpty()) {
            System.out.println("  No indirect connections found (only direct).");
        } else {
            int show = Math.min(5, itineraries.size());
            for (int i = 0; i < show; i++) {
                Itinerary it = itineraries.get(i);
                System.out.println("\n---- Option " + (i + 1) + " ----");
                System.out.println(it);
            }
        }

        System.out.println("\nDone with Issue 3 demo.");
    }
}