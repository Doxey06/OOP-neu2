package verwaltung;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.*;

/**
 * Verwaltungsklasse für automatische Benachrichtigungen
 */
public class BenachrichtigungsVerwaltung {
    private List<Benachrichtigung> benachrichtigungen;
    private int naechsteId = 1;
    
    public BenachrichtigungsVerwaltung() {
        this.benachrichtigungen = new ArrayList<>();
    }
    
    /**
     * Erstellt automatische Erinnerungen für anstehende Fristen
     */
    public void erstelleFristerinnerungen(List<Student> studenten, List<Klausur> klausuren) {
        LocalDate heute = LocalDate.now();
        
        for (Klausur klausur : klausuren) {
            long tageZurFrist = ChronoUnit.DAYS.between(heute, klausur.getAnmeldefrist());
            
            // Warnung 7 Tage vor Fristablauf
            if (tageZurFrist == 7) {
                for (Student student : studenten) {
                    String nachricht = "⚠️ Die Anmeldefrist für '" + klausur.getTitel() + 
                                     "' läuft in 7 Tagen ab! (Frist: " + klausur.getAnmeldefrist() + ")";
                    
                    Benachrichtigung benachrichtigung = new Benachrichtigung(
                            "FRIST_" + naechsteId++, 
                            student, 
                            nachricht, 
                            Benachrichtigung.BenachrichtigungsTyp.FRISTERINNERUNG
                    );
                    
                    hinzufuegen(benachrichtigung);
                    benachrichtigung.sende();
                }
            }
            
            // Finale Warnung 1 Tag vor Fristablauf
            if (tageZurFrist == 1) {
                for (Student student : studenten) {
                    String nachricht = "🚨 LETZTE CHANCE! Anmeldefrist für '" + klausur.getTitel() + 
                                     "' läuft MORGEN ab! Jetzt anmelden!";
                    
                    Benachrichtigung benachrichtigung = new Benachrichtigung(
                            "URGENT_" + naechsteId++, 
                            student, 
                            nachricht, 
                            Benachrichtigung.BenachrichtigungsTyp.WARNUNG
                    );
                    
                    hinzufuegen(benachrichtigung);
                    benachrichtigung.sende();
                }
            }
        }
    }
    
    /**
     * Erstellt Benachrichtigung über neue Klausuranmeldung
     */
    public void benachrichtigeKlausuranmeldung(Student student, Klausur klausur) {
        String nachricht = "✅ Erfolgreich für '" + klausur.getTitel() + "' angemeldet. " +
                          "Klausurtermin: " + klausur.getDatum().toLocalDate() + " in " + klausur.getRaum();
        
        Benachrichtigung benachrichtigung = new Benachrichtigung(
                "ANMELD_" + naechsteId++,
                student,
                nachricht,
                Benachrichtigung.BenachrichtigungsTyp.KLAUSUR_ANMELDUNG
        );
        
        hinzufuegen(benachrichtigung);
        benachrichtigung.sende();
    }
    
    /**
     * Benachrichtigt über verfügbare Noten
     */
    public void benachrichtigeNotenVerfuegbar(Student student, Versuch versuch) {
        String nachricht = "📊 Note für '" + versuch.getKlausur().getTitel() + "' ist verfügbar: " +
                          versuch.getNote() + " (" + versuch.getBewertung() + ")";
        
        Benachrichtigung benachrichtigung = new Benachrichtigung(
                "NOTE_" + naechsteId++,
                student,
                nachricht,
                Benachrichtigung.BenachrichtigungsTyp.NOTE_VERFUEGBAR
        );
        
        hinzufuegen(benachrichtigung);
        benachrichtigung.sende();
    }
    
    /**
     * Fügt eine Benachrichtigung hinzu
     */
    public void hinzufuegen(Benachrichtigung benachrichtigung) {
        benachrichtigungen.add(benachrichtigung);
    }
    
    /**
     * Gibt alle Benachrichtigungen für einen Studenten zurück
     */
    public List<Benachrichtigung> getBenachrichtigungenFuerStudent(Student student) {
        return benachrichtigungen.stream()
                .filter(b -> b.getEmpfaenger().equals(student))
                .sorted((b1, b2) -> b2.getZeitpunkt().compareTo(b1.getZeitpunkt())) // Neueste zuerst
                .collect(Collectors.toList());
    }
    
    /**
     * Gibt ungelesene Benachrichtigungen für einen Studenten zurück
     */
    public List<Benachrichtigung> getUngelesene(Student student) {
        return benachrichtigungen.stream()
                .filter(b -> b.getEmpfaenger().equals(student) && !b.istGelesen())
                .sorted((b1, b2) -> b2.getZeitpunkt().compareTo(b1.getZeitpunkt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Markiert alle Benachrichtigungen eines Studenten als gelesen
     */
    public void alleAlsGelesenMarkieren(Student student) {
        benachrichtigungen.stream()
                .filter(b -> b.getEmpfaenger().equals(student))
                .forEach(Benachrichtigung::markiereAlsGelesen);
    }
}
