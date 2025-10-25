package app;

import domain.*;
import infra.*;
import java.time.LocalDate;
import java.util.List;

public class TestBookingMain {

    public static void main(String[] args) throws Exception {
        // 1) Wire in-memory repositories
        ClientRepository clients = new InMemoryClientRepository();
        TripRepository trips = new InMemoryTripRepository();
        ReservationRepository reservations = new InMemoryReservationRepository();
        TicketRepository tickets = new InMemoryTicketRepository();
        BookingService booking = new BookingService(clients, trips, reservations, tickets);

        // 2) Load network & pick a real route to build an Itinerary
        TrainNetwork net = new TrainNetwork();
        net.load("./resources/eu_rail_network.csv");

        // Example: try to find a real direct route; change cities if needed
        String from = "Paris";
        String to = "Amsterdam";
        Route r = net.getRoute(from, to);
        if (r == null) {
            // fallback: just pick ANY route departing from Paris so the test always runs
            List<Route> parisRoutes = net.getRoutesFrom("Paris");
            if (parisRoutes.isEmpty()) {
                throw new IllegalStateException("No routes found from Paris in CSV");
            }
            r = parisRoutes.get(0);
            to = r.getArrivalCity();
            System.out.println("[WARN] No direct Paris→Amsterdam; using Paris→" + to + " instead.");
        }

        // Build a 1-leg itinerary from that route
        Itinerary itin = new Itinerary();
        itin.addLeg(new Leg(r, /*transferFromPrev*/ 0, r.getDurationMinutes()));
        itin.recomputeTotals();

        // 3) Register a client
        Client c = booking.registerClient("Alice Example", "alice@example.com", "555-0100");
        System.out.println("Client created: " + c);

        // 4) Create a trip for that client
        Trip trip = booking.createTrip(
                c.getClientId(),
                itin,
                LocalDate.now().plusDays(10),
                Trip.FareClass.SECOND,
                /*passengers*/ 1
        );
        System.out.println("Trip created: " + trip);

        // 5) Add a reservation to that trip (unconfirmed)
        Reservation res = booking.addReservation(
                trip.getTripId(),
                c.getClientId(),
                "Alice Example",
                17,
                "A1",
                Trip.FareClass.SECOND
        );
        System.out.println("Reservation added (unconfirmed): " + res);

        // 6) Confirm reservation -> issues a unique ticket & (optionally) flip trip to ACTIVE
        Ticket t = booking.confirmReservation(res.getReservationId());
        System.out.println("Ticket issued: " + t);

        // 7) Fetch back what we saved — smoke checks
        System.out.println("\n— Sanity checks —");
        System.out.println("Trips of client: " + booking.findTripsOfClient(c.getClientId()).size());
        System.out.println("Trip by id    : " + booking.findTrip(trip.getTripId()).orElse(null));
        System.out.println("Trip reservations: " + booking.reservationsOfTrip(trip.getTripId()).size());

        // 8) Negative tests (each should throw)
        try {
            booking.createTrip("NO-SUCH-CLIENT", itin, LocalDate.now(), Trip.FareClass.SECOND, 1);
            System.out.println("ERROR: expected exception for missing client");
        } catch (IllegalArgumentException expected) {
            System.out.println("OK: missing client rejected: " + expected.getMessage());
        }

        try {
            booking.createTrip(c.getClientId(), new Itinerary(), LocalDate.now(), Trip.FareClass.SECOND, 1);
            System.out.println("ERROR: expected exception for empty itinerary");
        } catch (IllegalArgumentException expected) {
            System.out.println("OK: empty itinerary rejected: " + expected.getMessage());
        }

        try {
            booking.createTrip(c.getClientId(), itin, LocalDate.now(), Trip.FareClass.SECOND, 0);
            System.out.println("ERROR: expected exception for passengers < 1");
        } catch (IllegalArgumentException expected) {
            System.out.println("OK: invalid pax rejected: " + expected.getMessage());
        }
    }
}
