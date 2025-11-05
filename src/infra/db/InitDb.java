package infra.db;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Creates/updates the SQLite schema used by the app.
 * Safe to run multiple times.
 */
public final class InitDb {

    private InitDb() {}

    /** Create all tables if they don't exist. */
    public static void createTables() {
        try (Connection conn = Database.get();
             Statement stmt = conn.createStatement()) {

            // --- Reference data for the route network ---
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cities (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS routes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    departure_city TEXT NOT NULL,
                    arrival_city   TEXT NOT NULL,
                    dep_time       TEXT NOT NULL,   -- HH:mm
                    arr_time       TEXT NOT NULL,   -- HH:mm
                    train_type     TEXT NOT NULL,
                    duration_min   INTEGER NOT NULL,
                    price_first    INTEGER NOT NULL,
                    price_second   INTEGER NOT NULL,
                    UNIQUE (departure_city, arrival_city, dep_time, arr_time, train_type)
                );
            """);

            // --- Domain tables for booking flow ---
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Client (
                    clientId TEXT PRIMARY KEY,
                    name     TEXT NOT NULL,
                    email    TEXT NOT NULL,
                    phone    TEXT
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Trip (
                    tripId         TEXT PRIMARY KEY,
                    clientId       TEXT NOT NULL,
                    tripDate       TEXT NOT NULL,   -- ISO yyyy-MM-dd
                    fareClass      TEXT NOT NULL,   -- FIRST / SECOND
                    passengerCount INTEGER NOT NULL,
                    status         TEXT NOT NULL,   -- SCHEDULED / CANCELLED / COMPLETED
                    FOREIGN KEY (clientId) REFERENCES Client(clientId)
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Reservation (
                    reservationId     TEXT PRIMARY KEY,
                    tripId            TEXT NOT NULL,
                    clientId          TEXT NOT NULL,
                    passengerName     TEXT NOT NULL,
                    passengerAge      INTEGER NOT NULL,
                    passengerIdNumber TEXT NOT NULL,
                    fareClass         TEXT NOT NULL,
                    confirmed         INTEGER DEFAULT 0,
                    FOREIGN KEY (tripId)  REFERENCES Trip(tripId),
                    FOREIGN KEY (clientId) REFERENCES Client(clientId)
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Ticket (
                    ticketId        TEXT PRIMARY KEY,
                    reservationId   TEXT NOT NULL,
                    tripId          TEXT NOT NULL,
                    clientId        TEXT NOT NULL,
                    passengerName   TEXT NOT NULL,
                    fareClass       TEXT NOT NULL,
                    totalPriceCents INTEGER NOT NULL,
                    issuedAt        TEXT NOT NULL,  -- ISO-8601 instant
                    FOREIGN KEY (reservationId) REFERENCES Reservation(reservationId),
                    FOREIGN KEY (tripId)        REFERENCES Trip(tripId)
                );
            """);

            System.out.println("Tables created (if not existing).");

        } catch (Exception e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}