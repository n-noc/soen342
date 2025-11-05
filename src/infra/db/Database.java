package infra.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.sql.*;
import java.util.stream.Collectors;

public final class Database {
    private static final String URL = "jdbc:sqlite:./data/app.db";

    static {
        try {
            Files.createDirectories(Path.of("data"));
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void runSqlResource(String resourcePath) {
        try (Connection c = get(); Statement st = c.createStatement()) {
            String sql = readResource(resourcePath);
            // Split on semicolons that end statements
            for (String stmt : sql.split(";\\s*\\n")) {
                var s = stmt.trim();
                if (!s.isEmpty()) st.execute(s);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run SQL from " + resourcePath, e);
        }
    }

    private static String readResource(String path) throws Exception {
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("Resource not found: " + path);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}