package infra;

import domain.Reservation;
import java.util.Collection;

public interface ReservationRepository {
    void save(Reservation reservation);
    Reservation findById(String reservationId);
    Collection<Reservation> findAll();
    Collection<Reservation> findByTripId(String tripId);
    boolean exists(String reservationId);
}