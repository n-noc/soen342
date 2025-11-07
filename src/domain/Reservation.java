// domain/Reservation.java
package domain;

import infra.TripRepository;
import java.util.UUID;

public class Reservation {

    private final String reservationId;
    private final String tripId;
    private final String clientId;
    private final String passengerName;
    private final int passengerAge;
    private final String passengerIdNumber;
    private final Trip.FareClass fareClass;

    private boolean confirmed;
    private Ticket ticket;

    public Reservation(String tripId, String clientId, String passengerName, int passengerAge, String passengerIdNumber, Trip.FareClass fareClass) {
        this.reservationId = UUID.randomUUID().toString();
        this.tripId = tripId;
        this.clientId = clientId;
        this.passengerName = passengerName;
        this.passengerAge = passengerAge;
        this.passengerIdNumber = passengerIdNumber;
        this.fareClass = fareClass;
        this.confirmed = false;
        this.ticket = null;
    }

    // constructor for the sql repo
   public Reservation(String reservationId, String tripId, String clientId, String passengerName,
                        int passengerAge, String passengerIdNumber, Trip.FareClass fareClass, boolean confirmed) {
        this.reservationId = reservationId; 
        this.tripId = tripId;
        this.clientId = clientId;
        this.passengerName = passengerName;
        this.passengerAge = passengerAge;
        this.passengerIdNumber = passengerIdNumber;
        this.fareClass = fareClass;
        this.confirmed = confirmed;
        this.ticket = null;  // default, since the repo doesn’t auto-load tickets
    }


    // Confirm reservation; verify trip exists, compute price, issue ticket. */
    public void confirm(TripRepository tripRepo) {
        Trip trip = tripRepo.findById(tripId);
        if (trip == null) {
            throw new IllegalStateException("Trip not found: " + tripId);
        }
        if (trip.getStatus() == Trip.TripStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm reservation for a cancelled trip: " + tripId);
        }
        this.confirmed = true;
        this.ticket = new Ticket(this, trip.totalPrice());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public int getPassengerAge() {
        return passengerAge;
    }

    public String getPassengerIdNumber() {
        return passengerIdNumber;
    }

    public Trip.FareClass getFareClass() {
        return fareClass;
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public String toString() {
        return "Reservation{id=%s, client=%s, trip=%s, name=%s, age=%d, id=%s, class=%s, confirmed=%s}"
                .formatted(reservationId, clientId, tripId, passengerName, passengerAge, passengerIdNumber, fareClass, confirmed);
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }   
}
