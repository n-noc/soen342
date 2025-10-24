package app;

import domain.Client;
import domain.Itinerary;
import domain.Reservation;
import domain.Ticket;
import domain.Trip;
import infra.ClientRepository;
import infra.ReservationRepository;
import infra.TicketRepository;
import infra.TripRepository;

import java.time.LocalDate;
import java.util.*;

public class BookingService {

    private final ClientRepository clients;
    private final TripRepository trips;
    private final ReservationRepository reservations;
    private final TicketRepository tickets;

    public BookingService(ClientRepository clients,
                          TripRepository trips,
                          ReservationRepository reservations,
                          TicketRepository tickets) {
        this.clients = Objects.requireNonNull(clients);
        this.trips = Objects.requireNonNull(trips);
        this.reservations = Objects.requireNonNull(reservations);
        this.tickets = Objects.requireNonNull(tickets);
    }

    /** Registers a new client. */
    public Client registerClient(String name, String email, String phone) {
        require(!isBlank(name), "name is required");
        require(!isBlank(email), "email is required");

        Client c = new Client(name.trim(), email.trim(), phone == null ? "" : phone.trim());
        clients.save(c);
        return c;
    }

    public Optional<Client> findClient(String clientId) {
        return Optional.ofNullable(clients.findById(clientId));
    }

    //Creates a Trip for an existing client.
    //- client must exist
    //- itinerary not null and must contain ≥ 1 leg
    //- passengerCount >= 1
    //- fareClass defaults to SECOND when null
    public Trip createTrip(String clientId,
                           Itinerary itinerary,
                           LocalDate tripDate,
                           Trip.FareClass fareClass,
                           int passengerCount) {

        requireClientExists(clientId);
        require(itinerary != null && !itinerary.getLegs().isEmpty(), "itinerary is required and must have legs");
        require(tripDate != null, "tripDate is required");
        require(passengerCount >= 1, "passengerCount must be ≥ 1");

        Trip trip = new Trip(
                clientId,
                itinerary,
                tripDate,
                fareClass == null ? Trip.FareClass.SECOND : fareClass,
                Math.max(1, passengerCount)
        );
        // Trip starts as SCHEDULED (follows enum)
        trips.save(trip);
        return trip;
    }

    /** Cancels a trip. */
    public void cancelTrip(String tripId) {
        Trip trip = requireTrip(tripId);
        trip.setStatus(Trip.TripStatus.CANCELLED);
        trips.save(trip);
    }

    public Optional<Trip> findTrip(String tripId) {
        return Optional.ofNullable(trips.findById(tripId));
    }

    public Collection<Trip> findTripsOfClient(String clientId) {
        // Ensure your TripRepository defines this method
        return trips.findByClientId(clientId);
    }

    //Adds a reservation to a trip but doesnt confirm it yet (no ticket)
    public Reservation addReservation(String tripId,
                                      String clientId,
                                      String passengerName,
                                      Trip.FareClass fareClass) {
        Trip trip = requireTrip(tripId);
        requireClientExists(clientId);
        require(!isBlank(passengerName), "passengerName is required");

        Reservation res = new Reservation(
                trip.getTripId(),
                clientId,
                passengerName,
                fareClass == null ? Trip.FareClass.SECOND : fareClass
        );

        reservations.save(res);
        return res;
    }

    // Confirms a reservation and issues a unique Ticket.
    public Ticket confirmReservation(String reservationId) {
        Reservation res = requireReservation(reservationId);
        if (res.isConfirmed()) return res.getTicket();
    
        res.confirm(trips);   
    
        Ticket ticket = res.getTicket();
        ticket = ensureUniqueTicket(ticket, res);
        reservations.save(res);
        tickets.save(ticket);
        trips.save(trips.findById(res.getTripId())); 
        return ticket;
    }

    public Collection<Reservation> reservationsOfTrip(String tripId) {
        return reservations.findByTripId(tripId);
    }

    // helpers

    private Trip requireTrip(String tripId) {
        Trip t = trips.findById(tripId);
        require(t != null, "trip not found: " + tripId);
        return t;
    }

    private Reservation requireReservation(String reservationId) {
        Reservation r = reservations.findById(reservationId);
        require(r != null, "reservation not found: " + reservationId);
        return r;
    }

    private void requireClientExists(String clientId) {
        boolean exists = clients.exists(clientId);
        if (!exists && clients.findById(clientId) != null) exists = true; // fallback if repo doesn't implement exists
        require(exists, "client not found: " + clientId);
    }

    private Ticket ensureUniqueTicket(Ticket ticket, Reservation resCtx) {
        String id = ticket.getTicketId();
        int guard = 0;
        while (tickets.existsById(id)) {          
            if (++guard > 5) throw new IllegalStateException("Unable to generate unique ticket id");
            // regenerate ticket with a new random id (new Ticket creates new UUID)
            ticket = new Ticket(resCtx, ticket.getTotalPriceCents());
            id = ticket.getTicketId();
        }
        return ticket;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}