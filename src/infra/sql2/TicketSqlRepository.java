package infra.sql2;

import domain.Ticket;
import domain.Trip;
import infra.TicketRepository;
import infra.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TicketSqlRepository implements TicketRepository {

    @Override
    public void save(Ticket t) {
        String sql = """
            INSERT INTO Ticket(
                ticketId, reservationId, tripId, clientId,
                passengerName, fareClass, totalPriceCents, issuedAt
            ) VALUES (?,?,?,?,?,?,?,?)
            ON CONFLICT(ticketId) DO UPDATE SET
                reservationId     = excluded.reservationId,
                tripId            = excluded.tripId,
                clientId          = excluded.clientId,
                passengerName     = excluded.passengerName,
                fareClass         = excluded.fareClass,
                totalPriceCents   = excluded.totalPriceCents,
                issuedAt          = excluded.issuedAt
            """;

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getTicketId());
            ps.setString(2, t.getReservationId());
            ps.setString(3, t.getTripId());
            ps.setString(4, t.getClientId());
            ps.setString(5, t.getPassengerName());
            ps.setString(6, t.getFareClass().name());
            ps.setInt(7, t.getTotalPriceCents());
            ps.setString(8, t.getIssuedAt().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ticket save() failed", e);
        }
    }

    @Override
    public Ticket findById(String ticketId) {
        String sql = "SELECT * FROM Ticket WHERE ticketId = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ticket findById failed", e);
        }
    }

    @Override
    public Collection<Ticket> findAll() {
        String sql = "SELECT * FROM Ticket ORDER BY issuedAt DESC";
        List<Ticket> out = new ArrayList<>();
        try (Connection c = Database.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ticket findAll failed", e);
        }
        return out;
    }

    @Override
    public boolean existsById(String ticketId) {
        String sql = "SELECT 1 FROM Ticket WHERE ticketId = ? LIMIT 1";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ticket existsById() failed", e);
        }
    }

    // --- mapper ---
    private static Ticket map(ResultSet rs) throws SQLException {
        return new Ticket(
            rs.getString("ticketId"),
            rs.getString("reservationId"),
            rs.getString("tripId"),
            rs.getString("clientId"),
            rs.getString("passengerName"),
            Trip.FareClass.valueOf(rs.getString("fareClass")),
            rs.getInt("totalPriceCents"),
            rs.getString("issuedAt")
        );
    }
}