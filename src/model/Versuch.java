package model;

import java.time.LocalDate;

/**
 * Repräsentiert einen konkreten Prüfungsversuch
 */
public class Versuch implements Comparable<Versuch> {
    private Student student;
    private Klausur klausur;
    private double note;
    private LocalDate datum;
    private boolean erfolgreich;
    
    public Versuch(Student student, Klausur klausur, double note, LocalDate datum) {
        this.student = student;
        this.klausur = klausur;
        this.note = note;
        this.datum = datum;
        this.erfolgreich = istBestanden();
    }
    
    // Getter
    public Student getStudent() { return student; }
    public Klausur getKlausur() { return klausur; }
    public double getNote() { return note; }
    public LocalDate getDatum() { return datum; }
    public boolean isErfolgreich() { return erfolgreich; }
    
    /**
     * Prüft, ob der Versuch bestanden wurde (Note <= 4.0)
     */
    public boolean istBestanden() {
        return note >= 1.0 && note <= 4.0;
    }
    
    /**
     * Gibt eine textuelle Bewertung der Note zurück
     */
    public String getBewertung() {
        if (!istBestanden()) return "Nicht bestanden";
        if (note <= 1.3) return "Sehr gut";
        if (note <= 1.7) return "Gut";
        if (note <= 2.3) return "Gut";
        if (note <= 2.7) return "Befriedigend";
        if (note <= 3.3) return "Befriedigend";
        if (note <= 3.7) return "Ausreichend";
        if (note <= 4.0) return "Ausreichend";
        return "Nicht bestanden";
    }
    
    @Override
    public int compareTo(Versuch other) {
        return this.datum.compareTo(other.datum);
    }
    
    @Override
    public String toString() {
        return klausur.getTitel() + ": " + note + " (" + getBewertung() + ") am " + datum;
    }
}
