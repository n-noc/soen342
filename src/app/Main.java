package app;

import domain.Itinerary;
import domain.ItineraryComparators;
import domain.Route;
import domain.TrainConnection;
import infra.TrainNetwork;
import search.SearchQuery;
import search.SearchService;
import search.IndirectSearchService;

import java.io.IOException;
import java.util.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static TrainNetwork net = new TrainNetwork();
    private static boolean dataLoaded = false;

    public static void main(String[] args) {
        showBanner();

        int choice;
        do {
            showMenu();
            System.out.print("Enter your choice: ");
            choice = getIntInput();

            switch (choice) {
                case 1 -> loadNetwork();
                case 2 -> viewCitiesAndRoutes();
                case 3 -> findDeparturesFromCity();
                case 4 -> lookupDirectRoute();
                case 5 -> searchRoutesWithFilters();
                case 6 -> viewIndirectItineraries();
                case 0 -> System.out.println("Exiting system. Goodbye!");
                default -> System.out.println("❌ Invalid option. Try again.");
            }

        } while (choice != 0);
    }

//  menu methods

    private static void showBanner() {
        System.out.println("""
                ==========================================
                   🚆  EU Rail Itinerary System 🚆
                ==========================================
                Welcome! This system allows you to:
                - Explore train routes and schedules
                - Search and compare available trips
                - View direct and indirect itineraries
                ==========================================
                """);
    }

    private static void showMenu() {
        System.out.println("""
                MAIN MENU
                ==========================================
                1. Load train network data 
                2. View all cities and sample routes 
                3. Find routes departing from a city 
                4. Look up a direct route between two cities 
                5. Search routes using custom filters 
                6. View itineraries including transfers 
                ------------------------------------------
                0. Exit system
                ==========================================
                """);
    }

    private static void loadNetwork() {
        System.out.println("Loading train network...");
        try {
            net.load("./resources/eu_rail_network.csv");
            dataLoaded = true;
            System.out.println("✅ Data loaded successfully!");
            System.out.println("Total routes: " + net.getAllRoutes().size());
            System.out.println("Total connections: " + net.getAllConnections().size());
        } catch (IOException e) {
            System.err.println("❌ Failed to load CSV: " + e.getMessage());
        }
    }

    private static void viewCitiesAndRoutes() {
        ensureDataLoaded();
        System.out.println("\n=== Sample Routes ===");
        net.getAllRoutes().stream().limit(10).forEach(r ->
                System.out.printf("  %s → %s  dep %s  arr %s  type %s  dur %d min%n",
                        r.getDepartureCity(), r.getArrivalCity(),
                        r.getDepartureTime(), r.getArrivalTime(),
                        r.getTrainType(), r.getDurationMinutes()));
    }

    private static void findDeparturesFromCity() {
        ensureDataLoaded();
        System.out.print("Enter city name: ");
        String city = sc.nextLine().trim();
        List<TrainConnection> list = net.getDeparturesFrom(city);
        if (list.isEmpty()) {
            System.out.println("No departures found from " + city);
        } else {
            list.stream().limit(10).forEach(tc ->
                    System.out.printf("  %s → %s  %s → %s  type %s%n",
                            tc.getDepartureCity(), tc.getArrivalCity(),
                            tc.getDepartureTime(), tc.getArrivalTime(),
                            tc.getTraintype()));
        }
    }

    private static void lookupDirectRoute() {
        ensureDataLoaded();
        System.out.print("Enter departure city: ");
        String from = sc.nextLine().trim();
        System.out.print("Enter destination city: ");
        String to = sc.nextLine().trim();

        Route route = net.getRoute(from, to);
        if (route == null) {
            System.out.println("No direct route found between " + from + " and " + to);
        } else {
            System.out.printf("  %s → %s  dep %s  arr %s  dur %d min  type %s%n",
                    route.getDepartureCity(), route.getArrivalCity(),
                    route.getDepartureTime(), route.getArrivalTime(),
                    route.getDurationMinutes(), route.getTrainType());
        }
    }

    private static void searchRoutesWithFilters() {
        ensureDataLoaded();
    
        System.out.print("Enter departure city: ");
        String from = sc.nextLine().trim();
    
        System.out.print("Destination city (optional, press Enter for any): ");
        String to = sc.nextLine().trim();
        if (to.isEmpty()) to = null;
    
        System.out.print("Earliest departure (HH:mm): ");
        String depStartRaw = sc.nextLine();
        System.out.print("Latest departure (HH:mm): ");
        String depEndRaw = sc.nextLine();
    
        String depStart = normalizeTimeInput(depStartRaw, "00:00");
        String depEnd   = normalizeTimeInput(depEndRaw,   "23:59");
    
        SearchQuery q = new SearchQuery(
                from, to,
                depStart, depEnd,
                null, null,           // arrival window
                null,                 // trainType
                null,                 // days
                "ANY", null,          // price class + max price
                "DURATION", "ASC"     // sort
        );
        q.normalize();
        q.validate();
    
        var results = SearchService.direct(net, q);
    
        System.out.println("\nResults: " + results.size());
        results.stream().limit(20).forEach(r -> System.out.printf(
                "  %s → %s  dep %s  arr %s  dur %d min  type %s  1st€%d  2nd€%d%n",
                r.getDepartureCity(), r.getArrivalCity(),
                r.getDepartureTime(), r.getArrivalTime(),
                r.getDurationMinutes(), r.getTrainType(),
                r.getFirstClassPrice(), r.getSecondClassPrice()
        ));
    }

    private static void viewIndirectItineraries() {
        ensureDataLoaded();
        System.out.print("Enter departure city: ");
        String from = sc.nextLine().trim();
        System.out.print("Enter destination city: ");
        String to = sc.nextLine().trim();

        SearchQuery q = new SearchQuery(
                from, to, "00:00", "23:59",
                null, null, null, null,
                "ANY", null, "DURATION", "ASC"
        );
        q.normalize(); q.validate();

        List<Itinerary> itineraries = IndirectSearchService.find(net, q, 2, 50).stream()
                .filter(it -> it.getLegs().size() > 1)
                .sorted(ItineraryComparators.BY_TOTAL_DURATION)
                .toList();

        System.out.println("Found multi-leg itineraries: " + itineraries.size());
        if (itineraries.isEmpty()) {
            System.out.println("  No indirect routes found.");
        } else {
            for (int i = 0; i < itineraries.size(); i++) {
                System.out.println("\n---- Option " + (i + 1) + " ----");
                System.out.println(itineraries.get(i));
            }
        }
    }

    // helper methods

    private static void ensureDataLoaded() {
        if (!dataLoaded) {
            System.out.println("⚠️ Please load the network data first (Option 1).");
            throw new IllegalStateException("Data not loaded yet.");
        }
    }

    private static int getIntInput() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String normalizeTimeInput(String s, String fallback) {
        if (s == null || s.isBlank()) return fallback;
        s = s.trim();
    
        // 8  -> 08:00
        if (s.matches("\\d{1,2}")) {
            int h = Integer.parseInt(s);
            return String.format("%02d:00", h);
        }
    
        // 800 or 1230 -> 08:00 or 12:30
        if (s.matches("\\d{3,4}")) {
            int n = Integer.parseInt(s);
            int h = n / 100;
            int m = n % 100;
            return String.format("%02d:%02d", h, m);
        }
    
        // 8:0, 8:5, 8:50, 12:5, etc -> HH:mm
        if (s.matches("\\d{1,2}:\\d{1,2}")) {
            String[] p = s.split(":");
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            return String.format("%02d:%02d", h, m);
        }
    
        // already HH:mm
        if (s.matches("\\d{2}:\\d{2}")) return s;
    
        // fallback if something odd was typed
        return fallback;
    }
}