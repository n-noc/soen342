package infra;

import domain.Ticket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTicketRepository implements TicketRepository {
    private final Map<String, Ticket> byId = new ConcurrentHashMap<>();

    @Override public void save(Ticket t) { byId.put(t.getTicketId(), t); }
    @Override public Ticket findById(String id) { return byId.get(id); }
    @Override public Collection<Ticket> findAll() { return Collections.unmodifiableCollection(byId.values()); }

    /** ticketId acts as the unique “code” */
    @Override
    public boolean existsById(String id) {
    return byId.containsKey(id);
}
}