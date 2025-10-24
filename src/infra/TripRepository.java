package infra;

import domain.Trip;
import java.util.Collection;

public interface TripRepository {
    void save(Trip trip);
    Trip findById(String tripId);
    Collection<Trip> findAll();
    Collection<Trip> findByClientId(String clientId);
    boolean exists(String tripId);
}