package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine Prüfung/Klausur
 */
public class Klausur implements Comparable<Klausur> {
    private String id;
    private String titel;
    private String modul;
    private LocalDateTime datum;
    private String raum;
    private int maxVersuche;
    private LocalDate anmeldefrist;
    private List<Student> teilnehmendeStudenten;
    private Dozent verantwortlicherDozent;
    
    public Klausur(String id, String titel, String modul, LocalDateTime datum, String raum, int maxVersuche, LocalDate anmeldefrist) {
        this.id = id;
        this.titel = titel;
        this.modul = modul;
        this.datum = datum;
        this.raum = raum;
        this.maxVersuche = maxVersuche;
        this.anmeldefrist = anmeldefrist;
        this.teilnehmendeStudenten = new ArrayList<>();
    }
    
    // Getter/Setter
    public String getId() { return id; }
    public String getTitel() { return titel; }
    public String getModul() { return modul; }
    public LocalDateTime getDatum() { return datum; }
    public String getRaum() { return raum; }
    public int getMaxVersuche() { return maxVersuche; }
    public LocalDate getAnmeldefrist() { return anmeldefrist; }
    
    public void setDatum(LocalDateTime datum) { this.datum = datum; }
    public void setRaum(String raum) { this.raum = raum; }
    public void setAnmeldefrist(LocalDate anmeldefrist) { this.anmeldefrist = anmeldefrist; }
    public void setVerantwortlicherDozent(Dozent dozent) { this.verantwortlicherDozent = dozent; }
    
    /**
     * Prüft, ob die Anmeldefrist abgelaufen ist
     */
    public boolean istFristAbgelaufen() {
        return LocalDate.now().isAfter(anmeldefrist);
    }
    
    /**
     * Prüft, ob ein Student zur Klausur zugelassen ist
     */
    public boolean istStudentZugelassen(Student student) {
        return teilnehmendeStudenten.contains(student);
    }
    
    /**
     * Prüft auf zeitliche Konflikte mit einer anderen Klausur
     */
    public boolean konfliktMit(Klausur andere) {
        if (andere == null) return false;
        
        // Konflikte wenn Klausuren am gleichen Tag stattfinden
        return this.datum.toLocalDate().equals(andere.datum.toLocalDate());
    }
    
    public void studentHinzufuegen(Student student) throws FristAbgelaufenException {
        if (istFristAbgelaufen()) {
            throw new FristAbgelaufenException("Anmeldefrist ist abgelaufen!");
        }
        if (!teilnehmendeStudenten.contains(student)) {
            teilnehmendeStudenten.add(student);
        }
    }
    
    public List<Student> getTeilnehmendeStudenten() {
        return new ArrayList<>(teilnehmendeStudenten);
    }
    
    @Override
    public int compareTo(Klausur other) {
        return this.datum.compareTo(other.datum);
    }
    
    @Override
    public String toString() {
        return titel + " (" + modul + ") - " + datum.toLocalDate() + " in " + raum;
    }
}
