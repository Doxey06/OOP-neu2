package verwaltung;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.*;

/**
 * Verwaltungsklasse für Prüfungsversuche
 */
public class VersuchsVerwaltung {
    private List<Versuch> versuche;
    
    public VersuchsVerwaltung() {
        this.versuche = new ArrayList<>();
    }
    
    /**
     * Registriert einen neuen Prüfungsversuch
     */
    public void versuchHinzufuegen(Versuch versuch) {
        versuche.add(versuch);
        // Auch dem Studenten hinzufügen
        versuch.getStudent().addVersuch(versuch);
    }
    
    /**
     * Gibt alle Versuche eines Studenten zurück
     */
    public List<Versuch> getVersucheFuerStudent(Student student) {
        return versuche.stream()
                .filter(v -> v.getStudent().equals(student))
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Gibt alle Versuche für eine Klausur zurück
     */
    public List<Versuch> getVersucheFuerKlausur(Klausur klausur) {
        return versuche.stream()
                .filter(v -> v.getKlausur().equals(klausur))
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Berechnet Statistiken für eine Klausur
     */
    public KlausurStatistik berechneStatistik(Klausur klausur) {
        List<Versuch> klausurVersuche = getVersucheFuerKlausur(klausur);
        
        if (klausurVersuche.isEmpty()) {
            return new KlausurStatistik(0, 0.0, 0.0, 0, 0);
        }
        
        int gesamtVersuche = klausurVersuche.size();
        int bestandeneVersuche = (int) klausurVersuche.stream().filter(Versuch::istBestanden).count();
        
        double durchschnittsnote = klausurVersuche.stream()
                .filter(Versuch::istBestanden)
                .mapToDouble(Versuch::getNote)
                .average()
                .orElse(0.0);
        
        double bestehendenquote = (double) bestandeneVersuche / gesamtVersuche * 100;
        
        return new KlausurStatistik(gesamtVersuche, durchschnittsnote, bestehendenquote, bestandeneVersuche, gesamtVersuche - bestandeneVersuche);
    }
    
    /**
     * Innere Klasse für Klausur-Statistiken
     */
    public static class KlausurStatistik {
        public final int gesamtVersuche;
        public final double durchschnittsnote;
        public final double bestehendenquote;
        public final int bestanden;
        public final int nichtBestanden;
        
        public KlausurStatistik(int gesamtVersuche, double durchschnittsnote, double bestehendenquote, int bestanden, int nichtBestanden) {
            this.gesamtVersuche = gesamtVersuche;
            this.durchschnittsnote = durchschnittsnote;
            this.bestehendenquote = bestehendenquote;
            this.bestanden = bestanden;
            this.nichtBestanden = nichtBestanden;
        }
        
        @Override
        public String toString() {
            return String.format("Versuche: %d | Bestanden: %d (%.1f%%) | Durchschnitt: %.2f", 
                    gesamtVersuche, bestanden, bestehendenquote, durchschnittsnote);
        }
    }
}
