package infra;

import domain.Ticket;
import java.util.Collection;

public interface TicketRepository {
    void save(Ticket ticket);
    Ticket findById(String ticketId);
    Collection<Ticket> findAll();

    boolean existsById(String ticketId);
}