package infra;

import domain.Client;
import java.util.*;

public interface ClientRepository {
    void save(Client c);
    Client findById(String clientId);
    Collection<Client> findAll();
}