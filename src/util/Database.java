package util;

import java.sql.*;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:klausurverwaltung.db";
    
    static {
        try {
            // Lade den SQLite Treiber explizit
            Class.forName("org.sqlite.JDBC");
            System.out.println("✅ SQLite Treiber geladen!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC Treiber nicht gefunden!");
            System.err.println("Stelle sicher, dass sqlite-jdbc.jar im Classpath ist.");
        }
    }
    
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}