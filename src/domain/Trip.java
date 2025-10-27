package domain;

import java.time.LocalDate;
import java.util.UUID;

public class Trip {

    public enum FareClass { FIRST, SECOND }
    public enum TripStatus { SCHEDULED, CANCELLED, COMPLETED }

    private final String tripId;
    private final String clientId;
    private Itinerary itinerary;
    private LocalDate tripDate;

    private FareClass fareClass;
    private int passengerCount;
    private TripStatus status;

    // constructor
    public Trip(String clientId, Itinerary itinerary, LocalDate tripDate,
                FareClass fareClass, int passengerCount) {
        this.tripId = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.itinerary = itinerary;
        this.tripDate = tripDate;
        this.fareClass = fareClass == null ? FareClass.SECOND : fareClass;
        this.passengerCount = Math.max(1, passengerCount);
        this.status = TripStatus.SCHEDULED;
    }

    // total duration of itinerary
    public int totalDurationMinutes() {
        return itinerary.getTotalDurationMinutes();
    }

    // price per passenger based on class choice
    public int pricePerPassenger() {
        return (fareClass == FareClass.FIRST)
                ? itinerary.getTotalFirstClassPrice()
                : itinerary.getTotalSecondClassPrice();
    }

    // total price (if there is more than 1 passenger)
    public int totalPrice() {
        return pricePerPassenger() * passengerCount;
    }

    // getters and setters
    public String getTripId() { return tripId; }
    public String getClientId() { return clientId; }
    public Itinerary getItinerary() { return itinerary; }
    public LocalDate getTripDate() { return tripDate; }
    public FareClass getFareClass() { return fareClass; }
    public int getPassengerCount() { return passengerCount; }
    public TripStatus getStatus() { return status; }

    public void setFareClass(FareClass fareClass) { this.fareClass = fareClass; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = Math.max(1, passengerCount); }
    public void setStatus(TripStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format(
                "Trip{id=%s, clientId=%s, date=%s, class=%s, pax=%d, total=%d, status=%s, legs=%d}",
                tripId, clientId, tripDate, fareClass, passengerCount, totalPrice(), status,
                itinerary.getLegs().size());
    }
}
