package infra;

import domain.Reservation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReservationRepository implements ReservationRepository {
    private final Map<String, Reservation> byId = new ConcurrentHashMap<>();
    private final Map<String, List<Reservation>> byTrip = new ConcurrentHashMap<>();

    @Override
    public void save(Reservation r) {
        byId.put(r.getReservationId(), r);
        byTrip.computeIfAbsent(r.getTripId(), k -> new ArrayList<>());
        List<Reservation> list = byTrip.get(r.getTripId());
        list.removeIf(x -> x.getReservationId().equals(r.getReservationId()));
        list.add(r);
    }

    @Override public Reservation findById(String id) { return byId.get(id); }

    @Override
    public Collection<Reservation> findAll() {
        return Collections.unmodifiableCollection(byId.values());
    }

    @Override
    public Collection<Reservation> findByTripId(String tripId) {
        return Collections.unmodifiableList(byTrip.getOrDefault(tripId, List.of()));
    }

    @Override public boolean exists(String id) { return byId.containsKey(id); }
}