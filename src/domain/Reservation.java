// domain/Reservation.java
package domain;

import infra.TripRepository;
import java.util.UUID;

public class Reservation {
    private final String reservationId;
    private final String tripId;
    private final String clientId;
    private final String passengerName;
    private final Trip.FareClass fareClass;

    private boolean confirmed;
    private Ticket ticket;

    public Reservation(String tripId, String clientId, String passengerName, Trip.FareClass fareClass) {
        this.reservationId = UUID.randomUUID().toString();
        this.tripId = tripId;
        this.clientId = clientId;
        this.passengerName = passengerName;
        this.fareClass = fareClass;
        this.confirmed = false;
        this.ticket = null;
    }

    /** Confirm reservation: verify trip exists, compute price, issue ticket. */
    public void confirm(TripRepository tripRepo) {
        Trip trip = tripRepo.findById(tripId);
        if (trip == null) {
            throw new IllegalStateException("Trip not found: " + tripId);
        }
        this.confirmed = true;
        this.ticket = new Ticket(this, trip.totalPrice());
    }

    public boolean isConfirmed() { return confirmed; }
    public String getReservationId() { return reservationId; }
    public String getTripId() { return tripId; }
    public String getClientId() { return clientId; }
    public String getPassengerName() { return passengerName; }
    public Trip.FareClass getFareClass() { return fareClass; }
    public Ticket getTicket() { return ticket; }

    @Override
    public String toString() {
        return "Reservation{id=%s, client=%s, trip=%s, name=%s, class=%s, confirmed=%s}"
                .formatted(reservationId, clientId, tripId, passengerName, fareClass, confirmed);
    }
}