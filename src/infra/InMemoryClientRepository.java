package infra;

import domain.Client;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryClientRepository implements ClientRepository {
    private final Map<String, Client> byId = new ConcurrentHashMap<>();

    @Override public void save(Client c) { byId.put(c.getClientId(), c); }
    @Override public Client findById(String id) { return byId.get(id); }
    @Override public Collection<Client> findAll() { return Collections.unmodifiableCollection(byId.values()); }
    @Override public boolean exists(String id) { return byId.containsKey(id); }
}