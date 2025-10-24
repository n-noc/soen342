package infra;

import domain.Trip;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTripRepository implements TripRepository {
    private final Map<String, Trip> byId = new ConcurrentHashMap<>();
    private final Map<String, List<Trip>> byClient = new ConcurrentHashMap<>();

    @Override
    public void save(Trip t) {
        byId.put(t.getTripId(), t);
        byClient.computeIfAbsent(t.getClientId(), k -> new ArrayList<>());
        // ensure one copy per id in the client list (simple replace)
        List<Trip> list = byClient.get(t.getClientId());
        list.removeIf(x -> x.getTripId().equals(t.getTripId()));
        list.add(t);
    }

    @Override public Trip findById(String id) { return byId.get(id); }

    @Override
    public Collection<Trip> findAll() {
        return Collections.unmodifiableCollection(byId.values());
    }

    @Override
    public Collection<Trip> findByClientId(String clientId) {
        return Collections.unmodifiableList(byClient.getOrDefault(clientId, List.of()));
    }

    @Override public boolean exists(String id) { return byId.containsKey(id); }
}