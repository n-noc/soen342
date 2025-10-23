package infra;

import domain.Reservation;
import java.util.*;

public interface ReservationRepository {
    void save(Reservation r);
    Reservation findById(String reservationId);
    Collection<Reservation> findAll();
    Collection<Reservation> findByClientId(String clientId);
}