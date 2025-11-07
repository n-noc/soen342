package domain;

import java.time.Instant;
import java.util.UUID;

public class Ticket {
    private final String ticketId;
    private final String reservationId;
    private final String tripId;
    private final String clientId;
    private final String passengerName;
    private final Trip.FareClass fareClass;
    private final int totalPriceCents; // or int euros if consistent with rest
    private final Instant issuedAt;

    public Ticket(Reservation r, int totalPrice) {
        this.ticketId = UUID.randomUUID().toString();
        this.reservationId = r.getReservationId();
        this.tripId = r.getTripId();
        this.clientId = r.getClientId();
        this.passengerName = r.getPassengerName();
        this.fareClass = r.getFareClass();
        this.totalPriceCents = totalPrice; // use same unit as Trip.totalPrice()
        this.issuedAt = Instant.now();
    }

    // constructor for the repo
    // domain/Ticket.java (add this constructor)
    public Ticket(String ticketId,
        String reservationId,
        String tripId,
        String clientId,
        String passengerName,
        Trip.FareClass fareClass,
        int totalPriceCents,
        String issuedAtIso) {
        this.ticketId = ticketId;
        this.reservationId = reservationId;
        this.tripId = tripId;
        this.clientId = clientId;
        this.passengerName = passengerName;
        this.fareClass = fareClass;
        this.totalPriceCents = totalPriceCents;
        this.issuedAt = java.time.Instant.parse(issuedAtIso);
    }
    public String getTicketId() { return ticketId; }
    public String getReservationId() { return reservationId; }
    public String getTripId() { return tripId; }
    public String getClientId() { return clientId; }
    public String getPassengerName() { return passengerName; }
    public Trip.FareClass getFareClass() { return fareClass; }
    public int getTotalPriceCents() { return totalPriceCents; }
    public Instant getIssuedAt() { return issuedAt; }

    @Override
    public String toString() {
        return "Ticket{id=%s, res=%s, trip=%s, client=%s, name=%s, class=%s, total=%d, issued=%s}"
                .formatted(ticketId, reservationId, tripId, clientId, passengerName, fareClass, totalPriceCents, issuedAt);
    }


}
