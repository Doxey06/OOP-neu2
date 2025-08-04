package model;

import java.time.LocalDateTime;

/**
 * Repräsentiert automatische Systemmeldungen
 */
public class Benachrichtigung {
    private String id;
    private Student empfaenger;
    private String nachricht;
    private LocalDateTime zeitpunkt;
    private BenachrichtigungsTyp typ;
    private boolean gelesen;
    
    public enum BenachrichtigungsTyp {
        FRISTERINNERUNG, KLAUSUR_ANMELDUNG, NOTE_VERFUEGBAR, WARNUNG
    }
    
    public Benachrichtigung(String id, Student empfaenger, String nachricht, BenachrichtigungsTyp typ) {
        this.id = id;
        this.empfaenger = empfaenger;
        this.nachricht = nachricht;
        this.typ = typ;
        this.zeitpunkt = LocalDateTime.now();
        this.gelesen = false;
    }
    
    // Getter/Setter
    public String getId() { return id; }
    public Student getEmpfaenger() { return empfaenger; }
    public String getNachricht() { return nachricht; }
    public LocalDateTime getZeitpunkt() { return zeitpunkt; }
    public BenachrichtigungsTyp getTyp() { return typ; }
    public boolean istGelesen() { return gelesen; }
    
    public void markiereAlsGelesen() {
        this.gelesen = true;
    }
    
    /**
     * Simuliert das Senden der Benachrichtigung
     */
    public void sende() {
        System.out.println("📧 Benachrichtigung an " + empfaenger.getVorname() + " " + empfaenger.getNachname() + ": " + nachricht);
    }
    
    @Override
    public String toString() {
        String status = gelesen ? "✓" : "📬";
        return status + " " + typ + ": " + nachricht + " (" + zeitpunkt.toLocalDate() + ")";
    }
}
