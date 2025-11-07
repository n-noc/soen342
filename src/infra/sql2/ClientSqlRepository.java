package infra.sql2;

import domain.Client;
import infra.ClientRepository;
import infra.db.Database;

import java.sql.*;
import java.util.*;

public class ClientSqlRepository implements ClientRepository {

    @Override
    public void save(Client c) {
        String upsert = """
            INSERT INTO Client(clientId, name, email, phone)
            VALUES(?,?,?,?)
            ON CONFLICT(clientId) DO UPDATE SET
              name=excluded.name, email=excluded.email, phone=excluded.phone
        """;
        try (Connection conn = Database.get();
             PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setString(1, c.getClientId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhoneNumber());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Client findById(String id) {
        String sql = "SELECT clientId,name,email,phone FROM Client WHERE clientId=?";
        try (Connection conn = Database.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Client c = new Client(rs.getString("name"),
                                      rs.getString("email"),
                                      rs.getString("phone"));
                
                return new ClientProxy(
                    rs.getString("clientId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean exists(String id) {
        String sql = "SELECT 1 FROM Client WHERE clientId=? LIMIT 1";
        try (Connection conn = Database.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Collection<Client> findAll() {
        String sql = "SELECT clientId,name,email,phone FROM Client";
        List<Client> out = new ArrayList<>();
        try (Connection conn = Database.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new ClientProxy(
                    rs.getString("clientId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                ));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // helper to return client with fixed id
    static class ClientProxy extends Client {
        public ClientProxy(String id, String name, String email, String phone) {
            super(name, email, phone);
            try {
                var f = Client.class.getDeclaredField("clientId");
                f.setAccessible(true);
                f.set(this, id);
            } catch (Exception ignored) {}
        }
    }
}