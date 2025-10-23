package infra;

import domain.Reservation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReservationRepository implements ReservationRepository {
    private final Map<String, Reservation> byId = new ConcurrentHashMap<>();
    private final Map<String, List<Reservation>> byClient = new ConcurrentHashMap<>();

    @Override
    public void save(Reservation r) {
        byId.put(r.getReservationId(), r);
        byClient.computeIfAbsent(r.getClientId(), k -> new ArrayList<>()).add(r);
    }

    @Override
    public Reservation findById(String reservationId) {
        return byId.get(reservationId);
    }

    @Override
    public Collection<Reservation> findAll() {
        return Collections.unmodifiableCollection(byId.values());
    }

    @Override
    public Collection<Reservation> findByClientId(String clientId) {
        return Collections.unmodifiableCollection(byClient.getOrDefault(clientId, List.of()));
    }
}