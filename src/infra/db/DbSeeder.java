package infra.db;

import domain.Route;
import infra.TrainNetwork;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads routes from the CSV via TrainNetwork and inserts into SQLite.
 * Populates: cities, routes.
 */
public final class DbSeeder {

    private DbSeeder() {}

    /**
     * Seed database with route data parsed by TrainNetwork from the provided CSV path.
     * @param csvPath path to CSV (e.g., "./resources/eu_rail_network.csv")
     */
    public static void seedRoutesFromCsv(String csvPath) {
        System.out.println("Seeding routes from: " + csvPath);

        try {
            // Use existing CSV pipeline
            TrainNetwork net = new TrainNetwork();
            net.load(csvPath);
            List<Route> routes = net.getAllRoutes();

            System.out.println("Loaded " + routes.size() + " routes from CSV.");
            if (routes.isEmpty()) {
                System.out.println("No routes found in CSV, aborting seed.");
                return;
            }

            try (Connection conn = Database.get()) {
                conn.setAutoCommit(false);

                // 1) Insert cities (dedupe in-memory)
                String insertCitySql = """
                    INSERT INTO cities(name) VALUES (?)
                    ON CONFLICT(name) DO NOTHING
                """;
                try (PreparedStatement psCity = conn.prepareStatement(insertCitySql)) {
                    Set<String> seen = new HashSet<>();
                    for (Route r : routes) {
                        String dep = r.getDepartureCity();
                        String arr = r.getArrivalCity();

                        if (dep != null && seen.add(dep)) {
                            psCity.setString(1, dep);
                            psCity.addBatch();
                        }
                        if (arr != null && seen.add(arr)) {
                            psCity.setString(1, arr);
                            psCity.addBatch();
                        }
                    }
                    psCity.executeBatch();
                }

                // 2) Insert routes
                String insertRouteSql = """
                    INSERT INTO routes
                        (departure_city, arrival_city, dep_time, arr_time,
                         train_type, duration_min, price_first, price_second)
                    VALUES (?,?,?,?,?,?,?,?)
                    ON CONFLICT(departure_city, arrival_city, dep_time, arr_time, train_type)
                    DO NOTHING
                """;
                try (PreparedStatement psRoute = conn.prepareStatement(insertRouteSql)) {
                    for (Route r : routes) {
                        psRoute.setString(1, r.getDepartureCity());
                        psRoute.setString(2, r.getArrivalCity());
                        psRoute.setString(3, r.getDepartureTime());
                        psRoute.setString(4, r.getArrivalTime());
                        psRoute.setString(5, r.getTrainType());
                        psRoute.setInt(6, r.getDurationMinutes());
                        psRoute.setInt(7, r.getFirstClassPrice());
                        psRoute.setInt(8, r.getSecondClassPrice());
                        psRoute.addBatch();
                    }
                    psRoute.executeBatch();
                }

                conn.commit();
                System.out.println("Database successfully seeded with " + routes.size() + " routes.");
            }

        } catch (Exception e) {
            System.out.println("Error seeding DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}