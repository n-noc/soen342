package infra;

import domain.Client;
import java.util.Collection;

public interface ClientRepository {
    void save(Client client);
    Client findById(String clientId);
    Collection<Client> findAll();
    boolean exists(String clientId);
}