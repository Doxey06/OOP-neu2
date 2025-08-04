package verwaltung;

import model.*;
import util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verwaltungsklasse für Klausuren mit ArrayList-basierter Speicherung
 */
public class KlausurVerwaltung {
    private List<Klausur> klausuren;
    
    public KlausurVerwaltung() {
        this.klausuren = new ArrayList<>();
        erstelleTabelleWennNichtVorhanden();
    }
    
    /**
     * Fügt eine neue Klausur hinzu
     */
    public void hinzufuegen(Klausur klausur) throws KlausurKonfliktException {
        // Prüfe auf Konflikte
        for (Klausur vorhandene : klausuren) {
            if (vorhandene.konfliktMit(klausur)) {
                throw new KlausurKonfliktException("Zeitkonflikt mit " + vorhandene.getTitel());
            }
        }
        
        klausuren.add(klausur);
        speichereInDatenbank(klausur);
    }
    
    /**
     * Entfernt eine Klausur
     */
    public boolean loeschen(String id) {
        boolean entfernt = klausuren.removeIf(k -> k.getId().equals(id));
        if (entfernt) {
            loescheAusDatenbank(id);
        }
        return entfernt;
    }
    
    /**
     * Sucht Klausuren nach Titel oder Modul
     */
    public List<Klausur> suchenNachTitel(String suchtext) {
        return klausuren.stream()
                .filter(k -> k.getTitel().toLowerCase().contains(suchtext.toLowerCase()) ||
                           k.getModul().toLowerCase().contains(suchtext.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Gibt alle Klausuren sortiert nach Datum zurück
     */
    public List<Klausur> getAlleSortiert() {
        return klausuren.stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Findet Klausuren in einem bestimmten Zeitraum
     */
    public List<Klausur> suchenNachZeitraum(LocalDate von, LocalDate bis) {
        return klausuren.stream()
                .filter(k -> {
                    LocalDate klausurDatum = k.getDatum().toLocalDate();
                    return !klausurDatum.isBefore(von) && !klausurDatum.isAfter(bis);
                })
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Gibt kommende Klausuren zurück (ab heute)
     */
    public List<Klausur> getKommendeKlausuren() {
        LocalDate heute = LocalDate.now();
        return klausuren.stream()
                .filter(k -> !k.getDatum().toLocalDate().isBefore(heute))
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Findet Klausur anhand der ID
     */
    public Klausur findeNachId(String id) {
        return klausuren.stream()
                .filter(k -> k.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    private void erstelleTabelleWennNichtVorhanden() {
        String sql = """
            CREATE TABLE IF NOT EXISTS klausur (
                id TEXT PRIMARY KEY,
                titel TEXT NOT NULL,
                modul TEXT NOT NULL,
                datum TEXT NOT NULL,
                raum TEXT,
                max_versuche INTEGER DEFAULT 3,
                anmeldefrist TEXT NOT NULL
            )
        """;
        
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen der Klausur-Tabelle: " + e.getMessage());
        }
    }
    
    private void speichereInDatenbank(Klausur klausur) {
        String sql = "INSERT INTO klausur (id, titel, modul, datum, raum, max_versuche, anmeldefrist) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, klausur.getId());
            pstmt.setString(2, klausur.getTitel());
            pstmt.setString(3, klausur.getModul());
            pstmt.setString(4, klausur.getDatum().toString());
            pstmt.setString(5, klausur.getRaum());
            pstmt.setInt(6, klausur.getMaxVersuche());
            pstmt.setString(7, klausur.getAnmeldefrist().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern der Klausur: " + e.getMessage());
        }
    }
    
    private void loescheAusDatenbank(String id) {
        String sql = "DELETE FROM klausur WHERE id = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen der Klausur: " + e.getMessage());
        }
    }
}
