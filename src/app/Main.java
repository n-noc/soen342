package app;

import domain.Itinerary;
import domain.ItineraryComparators;
import domain.Route;
import domain.TrainConnection;
import infra.TrainNetwork;
import java.io.IOException;
import java.util.*;
import search.IndirectSearchService;
import search.SearchQuery;
import search.SearchService;

public class Main {

    // ===== App state & helpers =====
    private static final Scanner sc = new Scanner(System.in);
    private static final TrainNetwork net = new TrainNetwork();
    private static boolean dataLoaded = false;

    public static void main(String[] args) {
        showBanner();

        int choice;
        do {
            showMenu();
            System.out.print("Enter your choice: ");
            choice = getIntInput();

            switch (choice) {
                case 1 ->
                    loadNetwork();
                case 2 ->
                    viewCitiesAndRoutes();
                case 3 ->
                    findDeparturesFromCity();
                case 4 ->
                    lookupDirectRoute();
                case 5 ->
                    searchRoutesWithFilters();
                case 6 ->
                    viewIndirectItineraries();
                case 0 ->
                    System.out.println("Exiting system. Goodbye!");
                default ->
                    System.out.println("❌ Invalid option. Try again.");
            }
        } while (choice != 0);
    }

    // ===== Menu UI =====
    private static void showBanner() {
        System.out.println("""
                ==========================================
                   EU Rail Itinerary System
                ==========================================
                Welcome! This system allows you to:
                • Explore train routes and schedules
                • Search and compare available trips
                • View direct and indirect itineraries
                ==========================================
                """);
    }

    private static void showMenu() {
        System.out.println("""
                MAIN MENU
                ==========================================
                1. Load train network data
                2. View sample routes
                3. Find departures from a city
                4. Look up a direct route (city → city)
                5. Search routes with filters & sorting
                6. View itineraries including transfers
                ------------------------------------------
                0. Exit
                ==========================================
                """);
    }

    // ===== Actions =====
    private static void loadNetwork() {
        System.out.println("Loading train network...");
        try {
            net.load("./resources/eu_rail_network.csv");
            dataLoaded = true;
            System.out.println("✅ Data loaded successfully!");
            System.out.println("Total connections: " + net.getAllConnections().size());
            System.out.println("Total routes (trips): " + net.getAllRoutes().size());
        } catch (IOException e) {
            System.err.println("❌ Failed to load CSV: " + e.getMessage());
        }
    }

    private static void viewCitiesAndRoutes() {
        if (!ensureDataLoaded()) {
            return;
        }
        System.out.println("\n=== Sample Routes ===");
        net.getAllRoutes().stream().limit(10).forEach(r
                -> System.out.printf("  %s → %s  dep %s  arr %s  type %s  dur %d min%n",
                        r.getDepartureCity(), r.getArrivalCity(),
                        r.getDepartureTime(), r.getArrivalTime(),
                        r.getTrainType(), r.getDurationMinutes()));
    }

    private static void findDeparturesFromCity() {
        if (!ensureDataLoaded()) {
            return;
        }

        System.out.print("Enter city name: ");
        String city = sc.nextLine().trim();

        List<TrainConnection> list = net.getDeparturesFrom(city);
        if (list.isEmpty()) {
            System.out.println("No departures found from " + city);
            return;
        }

        System.out.println("\nDepartures from " + city + ":");
        list.stream().limit(20).forEach(tc
                -> System.out.printf("  %s → %s  %s → %s  type %s%n",
                        tc.getDepartureCity(), tc.getArrivalCity(),
                        tc.getDepartureTime(), tc.getArrivalTime(),
                        tc.getTraintype()));
    }

    private static void lookupDirectRoute() {
        if (!ensureDataLoaded()) {
            return;
        }

        System.out.print("Enter departure city: ");
        String from = sc.nextLine().trim();
        System.out.print("Enter destination city: ");
        String to = sc.nextLine().trim();

        Route route = net.getRoute(from, to);
        if (route == null) {
            System.out.println("No direct route found between " + from + " and " + to);
            return;
        }

        System.out.printf("Direct:  %s → %s  dep %s  arr %s  dur %d min  type %s  1st€%d  2nd€%d%n",
                route.getDepartureCity(), route.getArrivalCity(),
                route.getDepartureTime(), route.getArrivalTime(),
                route.getDurationMinutes(), route.getTrainType(),
                route.getFirstClassPrice(), route.getSecondClassPrice());
    }

    private static void searchRoutesWithFilters() {
        if (!ensureDataLoaded()) {
            return;
        }

        System.out.print("Enter departure city: ");
        String from = sc.nextLine().trim();

        System.out.print("Destination city (optional, press Enter for ANY): ");
        String to = sc.nextLine().trim();
        if (to.isEmpty()) {
            to = null;
        }

        // Time window
        System.out.print("Earliest departure (HH:mm): ");
        String depStartRaw = sc.nextLine();
        System.out.print("Latest departure   (HH:mm): ");
        String depEndRaw = sc.nextLine();
        String depStart = normalizeTimeInput(depStartRaw, "00:00");
        String depEnd = normalizeTimeInput(depEndRaw, "23:59");

        // Optional train type
        System.out.print("Train type (optional, EX: TGV/ICE/RE; Enter for ANY): ");
        String trainType = sc.nextLine().trim();
        if (trainType.isBlank()) {
            trainType = null;
        }

        // Optional days (comma or space separated, 3-letter codes)
        System.out.print("Days (optional, EX: MON,TUE or MON WED; Enter for ANY): ");
        String daysRaw = sc.nextLine().trim();
        Set<String> days = parseDays(daysRaw); // may be null

        // Price filters
        System.out.print("Price class (ANY/FIRST/SECOND) [default ANY]: ");
        String priceClass = sc.nextLine().trim();
        if (priceClass.isBlank()) {
            priceClass = "ANY";
        }
        priceClass = priceClass.toUpperCase(Locale.ROOT);

        System.out.print("Max price (optional integer, Enter for none): ");
        Integer maxPrice = parseOptionalInt(sc.nextLine());

        // Sort options
        System.out.print("Sort by (DURATION | PRICE_FIRST | PRICE_SECOND) [default DURATION]: ");
        String sortBy = sc.nextLine().trim();
        if (sortBy.isBlank()) {
            sortBy = "DURATION";
        }
        sortBy = sortBy.toUpperCase(Locale.ROOT);

        System.out.print("Direction (ASC | DESC) [default ASC]: ");
        String sortDir = sc.nextLine().trim();
        if (sortDir.isBlank()) {
            sortDir = "ASC";
        }
        sortDir = sortDir.toUpperCase(Locale.ROOT);

        SearchQuery q = new SearchQuery(
                from, to,
                depStart, depEnd,
                null, null, //UNUSED
                trainType,
                days,
                priceClass, maxPrice,
                sortBy, sortDir
        );
        q.normalize();
        try {
            q.validate();
        } catch (IllegalArgumentException ex) {
            System.out.println("❌ Invalid input: " + ex.getMessage());
            return;
        }

        var results = SearchService.direct(net, q);
        if (results.isEmpty()) {
            System.out.println("No matches.");
            return;
        }

        System.out.println("\nResults: " + results.size());
        results.stream().limit(50).forEach(r -> System.out.printf(
                "  %s → %s  dep %s  arr %s  dur %d min  type %s  1st€%d  2nd€%d%n",
                r.getDepartureCity(), r.getArrivalCity(),
                r.getDepartureTime(), r.getArrivalTime(),
                r.getDurationMinutes(), r.getTrainType(),
                r.getFirstClassPrice(), r.getSecondClassPrice()
        ));
    }

    private static void viewIndirectItineraries() {
        if (!ensureDataLoaded()) {
            return;
        }

        System.out.print("Enter departure city: ");
        String from = sc.nextLine().trim();
        System.out.print("Enter destination city: ");
        String to = sc.nextLine().trim();

        // Let the user optionally narrow by earliest/latest departure for leg 1
        System.out.print("Earliest departure for first leg (HH:mm) [default 00:00]: ");
        String depStart = normalizeTimeInput(sc.nextLine(), "00:00");
        System.out.print("Latest departure for first leg (HH:mm) [default 23:59]: ");
        String depEnd = normalizeTimeInput(sc.nextLine(), "23:59");

        SearchQuery q = new SearchQuery(
                from, to,
                depStart, depEnd,
                null, null,
                null, null,
                "ANY", null,
                "DURATION", "ASC"
        );
        q.normalize();
        try {
            q.validate();
        } catch (IllegalArgumentException ex) {
            System.out.println("❌ Invalid input: " + ex.getMessage());
            return;
        }

        List<Itinerary> itins = IndirectSearchService.find(net, q, 2, 100)
                .stream()
                .filter(it -> it.getLegs().size() > 1) // only itineraries with transfers
                .sorted(ItineraryComparators.BY_TOTAL_DURATION)
                .toList();

        System.out.println("Found multi-leg itineraries: " + itins.size());
        if (itins.isEmpty()) {
            System.out.println("  No indirect routes found.");
            return;
        }

        // sort
        System.out.print("Sort by (DURATION | TRANSFERS | PRICE_FIRST | PRICE_SECOND | ARRIVAL) [default DURATION]: ");
        String sortKey = sc.nextLine().trim().toUpperCase(Locale.ROOT);
        Comparator<Itinerary> cmp = switch (sortKey) {
            case "TRANSFERS" ->
                ItineraryComparators.BY_TRANSFERS;
            case "PRICE_FIRST" ->
                ItineraryComparators.BY_FIRST_CLASS_PRICE;
            case "PRICE_SECOND" ->
                ItineraryComparators.BY_SECOND_CLASS_PRICE;
            case "ARRIVAL" ->
                ItineraryComparators.BY_ARRIVAL_TIME;
            default ->
                ItineraryComparators.BY_TOTAL_DURATION;
        };

        System.out.print("Direction (ASC | DESC) [default ASC]: ");
        String dir = sc.nextLine().trim().toUpperCase(Locale.ROOT);
        if ("DESC".equals(dir)) {
            itins = itins.stream().sorted(cmp.reversed()).toList(); 
        }else {
            itins = itins.stream().sorted(cmp).toList();
        }

        int i = 1;
        for (Itinerary it : itins) {
            System.out.println("\n---- Option " + (i++) + " ----");
            System.out.println(it);
        }
    }

    // utility methods
    private static boolean ensureDataLoaded() {
        if (!dataLoaded) {
            System.out.println("⚠️ Please load the network data first (Option 1).");
            return false;
        }
        return true;
    }

    private static int getIntInput() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String normalizeTimeInput(String s, String fallback) {
        if (s == null || s.isBlank()) {
            return fallback;
        }
        s = s.trim();

        // 8  -> 08:00
        if (s.matches("\\d{1,2}")) {
            int h = Integer.parseInt(s);
            return String.format("%02d:00", h);
        }
        // 800 or 1230 -> 08:00 or 12:30
        if (s.matches("\\d{3,4}")) {
            int n = Integer.parseInt(s);
            int h = n / 100, m = n % 100;
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
        if (s.matches("\\d{2}:\\d{2}")) {
            return s;
        }

        return fallback;
    }

    private static Set<String> parseDays(String raw) {
        if (raw == null || raw.isBlank()) {
            return null; // ANY

                }String[] tokens = raw.replace(',', ' ').trim().split("\\s+");
        Set<String> out = new HashSet<>();
        for (String t : tokens) {
            String d = t.trim().toUpperCase(Locale.ROOT);
            if (d.length() >= 3) {
                d = d.substring(0, 3);
            }
            if (List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").contains(d)) {
                out.add(d);
            }
        }
        return out.isEmpty() ? null : out;
    }

    private static Integer parseOptionalInt(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
