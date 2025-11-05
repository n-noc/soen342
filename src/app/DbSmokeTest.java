package app;
import java.sql.*;

public class DbSmokeTest {
    private static final String URL = "jdbc:sqlite:./data/app.db";

    public static void main(String[] args) throws Exception {
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {

            String[] tables = {"Route", "Client", "Trip", "Reservation", "Ticket"};

            System.out.println("=== Row counts ===");
            for (String t : tables) {
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + t)) {
                    int n = rs.next() ? rs.getInt(1) : 0;
                    System.out.printf("  %-12s %d%n", t + ":", n);
                } catch (SQLException e) {
                    System.out.printf("  %-12s (missing table)%n", t + ":");
                }
            }

            // Show a few sample Route rows to confirm data looks right
            System.out.println("\n=== Sample Route rows ===");
            try (ResultSet rs = st.executeQuery("""
                SELECT routeId, departureCity, arrivalCity, departureTime, arrivalTime, trainType
                FROM Route
                LIMIT 5
            """)) {
                while (rs.next()) {
                    System.out.printf("  [%s] %s → %s  %s→%s  %s%n",
                            rs.getString("routeId"),
                            rs.getString("departureCity"),
                            rs.getString("arrivalCity"),
                            rs.getString("departureTime"),
                            rs.getString("arrivalTime"),
                            rs.getString("trainType"));
                }
            } catch (SQLException e) {
                System.out.println("  (no Route table or no data)");
            }
        }
    }
}