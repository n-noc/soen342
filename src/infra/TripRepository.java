package infra;

import domain.Trip;
import java.util.*;

public interface TripRepository {
    void save(Trip trip);
    Trip findById(String tripId);
    boolean exists(String tripId);
    Collection<Trip> findAll();
}