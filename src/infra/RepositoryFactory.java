// src/infra/RepositoryFactory.java
package infra;

import infra.sql2.*;        // your *SqlRepository classes
import infra.db.InitDb;

public final class RepositoryFactory {
    public enum Mode { MEMORY, SQLITE }

    public static ClientRepository clients(Mode m) {
        return switch (m) {
            case MEMORY -> new InMemoryClientRepository();
            case SQLITE -> new ClientSqlRepository();
        };
    }
    public static TripRepository trips(Mode m) {
        return switch (m) {
            case MEMORY -> new InMemoryTripRepository();
            case SQLITE -> new TripSqlRepository();
        };
    }
    public static ReservationRepository reservations(Mode m) {
        return switch (m) {
            case MEMORY -> new InMemoryReservationRepository();
            case SQLITE -> new ReservationSqlRepository();
        };
    }
    public static TicketRepository tickets(Mode m) {
        return switch (m) {
            case MEMORY -> new InMemoryTicketRepository();
            case SQLITE -> new TicketSqlRepository();
        };
    }

    // ensure db schema exists wehn using SQLITE mode
    public static void ensureSchema(Mode m) {
        if (m == Mode.SQLITE) {
            infra.db.InitDb.createTables();
        }
    }
}