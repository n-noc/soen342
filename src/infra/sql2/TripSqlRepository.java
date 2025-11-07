package infra.sql2;

import domain.*;
import infra.TripRepository;
import infra.db.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TripSqlRepository implements TripRepository {

    @Override
    public void save(Trip t) {
        String upsertTrip = """
          INSERT INTO Trip(tripId, clientId, tripDate, fareClass, passengerCount, status)
          VALUES(?,?,?,?,?,?)
          ON CONFLICT(tripId) DO UPDATE SET
            clientId=excluded.clientId, tripDate=excluded.tripDate,
            fareClass=excluded.fareClass, passengerCount=excluded.passengerCount,
            status=excluded.status
        """;

        String deleteLegs = "DELETE FROM TripLeg WHERE tripId=?";
        String insertLeg = """
          INSERT INTO TripLeg(tripId, legIndex, depCity, arrCity, depTime, arrTime,
                              trainType, durationMin, priceFirst, priceSecond, transferFromPrev)
          VALUES(?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(upsertTrip)) {
                ps.setString(1, t.getTripId());
                ps.setString(2, t.getClientId());
                ps.setString(3, t.getTripDate().toString());
                ps.setString(4, t.getFareClass().name());
                ps.setInt(5, t.getPassengerCount());
                ps.setString(6, t.getStatus().name());
                ps.executeUpdate();
            }
            // replace legs
            try (PreparedStatement del = c.prepareStatement(deleteLegs)) {
                del.setString(1, t.getTripId());
                del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement(insertLeg)) {
                List<Leg> legs = t.getItinerary().getLegs();
                for (int i = 0; i < legs.size(); i++) {
                    Leg L = legs.get(i);
                    Route r = L.getRoute();
                    ins.setString(1, t.getTripId());
                    ins.setInt(2, i);
                    ins.setString(3, r.getDepartureCity());
                    ins.setString(4, r.getArrivalCity());
                    ins.setString(5, r.getDepartureTime());
                    ins.setString(6, r.getArrivalTime());
                    ins.setString(7, r.getTrainType());
                    ins.setInt(8, r.getDurationMinutes());
                    ins.setInt(9, r.getFirstClassPrice());
                    ins.setInt(10, r.getSecondClassPrice());
                    ins.setInt(11, L.getTransferFromPrevMinutes() > 0 ? 1 : 0);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            c.commit();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Trip findById(String id) {
        String sqlTrip = "SELECT tripId, clientId, tripDate, fareClass, passengerCount, status FROM Trip WHERE tripId=?";
        String sqlLegs = """
          SELECT legIndex,depCity,arrCity,depTime,arrTime,trainType,durationMin,priceFirst,priceSecond,transferFromPrev
          FROM TripLeg WHERE tripId=? ORDER BY legIndex
        """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sqlTrip)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                var clientId = rs.getString("clientId");
                var tripDate = LocalDate.parse(rs.getString("tripDate"));
                var fare     = Trip.FareClass.valueOf(rs.getString("fareClass"));
                var pax      = rs.getInt("passengerCount");
                var status   = Trip.TripStatus.valueOf(rs.getString("status"));

                Itinerary itin = new Itinerary();
                try (PreparedStatement pl = c.prepareStatement(sqlLegs)) {
                    pl.setString(1, id);
                    try (ResultSet rl = pl.executeQuery()) {
                        while (rl.next()) {
                            Route r = new Route(
                                rl.getString("depCity"),
                                rl.getString("arrCity"),
                                rl.getString("depTime"),
                                rl.getString("arrTime"),
                                rl.getString("trainType"),
                                rl.getInt("durationMin"),
                                rl.getInt("priceFirst"),
                                rl.getInt("priceSecond")
                            );
                            itin.addLeg(new Leg(r, rl.getInt("transferFromPrev"), r.getDurationMinutes()));
                        }
                    }
                }
                itin.recomputeTotals();

                Trip t = new Trip(clientId, itin, tripDate, fare, pax);
                t.setStatus(status);
                // force the original id if needed (like with Client) — add an id constructor or use a proxy trick.
                return TripIdProxy.withId(t, id);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Collection<Trip> findAll() {
        List<Trip> out = new ArrayList<>();
        String sql = "SELECT tripId FROM Trip";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(findById(rs.getString("tripId")));
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Collection<Trip> findByClientId(String clientId) {
        List<Trip> out = new ArrayList<>();
        String sql = "SELECT tripId FROM Trip WHERE clientId=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(findById(rs.getString("tripId")));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean exists(String id) {
        String sql = "SELECT 1 FROM Trip WHERE tripId=? LIMIT 1";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // Helper to force id onto Trip 
    static class TripIdProxy {
        static Trip withId(Trip t, String id) {
            try {
                var f = Trip.class.getDeclaredField("tripId");
                f.setAccessible(true);
                f.set(t, id);
            } catch (Exception ignored) {}
            return t;
        }
    }
}