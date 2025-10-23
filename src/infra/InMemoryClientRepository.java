package infra;

import domain.Client;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryClientRepository implements ClientRepository {
    private final Map<String, Client> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Client c) {
        byId.put(c.getClientId(), c);
    }

    @Override
    public Client findById(String clientId) {
        return byId.get(clientId);
    }

    @Override
    public Collection<Client> findAll() {
        return Collections.unmodifiableCollection(byId.values());
    }
}