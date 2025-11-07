package infra.sql2;

import domain.Reservation;
import domain.Trip;
import infra.ReservationRepository;
import infra.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReservationSqlRepository implements ReservationRepository {

    @Override
    public void save(Reservation r) {
        String sql = """
            INSERT INTO Reservation(
                reservationId, tripId, clientId,
                passengerName, passengerAge, passengerIdNumber,
                fareClass, confirmed
            ) VALUES (?,?,?,?,?,?,?,?)
            ON CONFLICT(reservationId) DO UPDATE SET
                tripId=excluded.tripId,
                clientId=excluded.clientId,
                passengerName=excluded.passengerName,
                passengerAge=excluded.passengerAge,
                passengerIdNumber=excluded.passengerIdNumber,
                fareClass=excluded.fareClass,
                confirmed=excluded.confirmed
            """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getReservationId());
            ps.setString(2, r.getTripId());
            ps.setString(3, r.getClientId());
            ps.setString(4, r.getPassengerName());
            ps.setInt(5, r.getPassengerAge());
            ps.setString(6, r.getPassengerIdNumber());
            ps.setString(7, r.getFareClass().name());
            ps.setInt(8, r.isConfirmed() ? 1 : 0);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Reservation save failed", e);
        }
    }

    @Override
    public Reservation findById(String reservationId) {
        String sql = "SELECT * FROM Reservation WHERE reservationId = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Reservation findById failed", e);
        }
    }

    @Override
    public Collection<Reservation> findByTripId(String tripId) {
        String sql = "SELECT * FROM Reservation WHERE tripId = ? ORDER BY reservationId";
        List<Reservation> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Reservation findByTripId failed", e);
        }
    }

    @Override
    public boolean exists(String reservationId) {
        String sql = "SELECT 1 FROM Reservation WHERE reservationId = ? LIMIT 1";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Reservation exists() failed", e);
        }
    }

    // mapper
    private static Reservation map(ResultSet rs) throws SQLException {
        String reservationId     = rs.getString("reservationId");
        String tripId            = rs.getString("tripId");
        String clientId          = rs.getString("clientId");
        String passengerName     = rs.getString("passengerName");
        int    passengerAge      = rs.getInt("passengerAge");
        String passengerIdNumber = rs.getString("passengerIdNumber");
        Trip.FareClass fareClass = Trip.FareClass.valueOf(rs.getString("fareClass"));
        boolean confirmed        = rs.getInt("confirmed") == 1;

        return new Reservation(
                reservationId, tripId, clientId,
                passengerName, passengerAge, passengerIdNumber,
                fareClass, confirmed
        );
    }
    @Override
public Collection<Reservation> findAll() {
    String sql = "SELECT * FROM Reservation ORDER BY reservationId";
    List<Reservation> out = new ArrayList<>();
    try (Connection c = Database.get();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) out.add(map(rs));
    } catch (SQLException e) {
        throw new RuntimeException("Reservation findAll failed", e);
    }
    return out;
}
}