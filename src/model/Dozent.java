package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dozent erbt von Person
 */
public class Dozent extends Person {
    private List<Klausur> zugeordneteKlausuren;
    private DozentenRolle rolle;
    
    public enum DozentenRolle {
        DOZENT, PROFESSOR, LEHRBEAUFTRAGTE, ADMIN
    }
    
    public Dozent(String id, String vorname, String nachname, LocalDate geburtsdatum, DozentenRolle rolle) {
        super(id, vorname, nachname, geburtsdatum);
        this.rolle = rolle;
        this.zugeordneteKlausuren = new ArrayList<>();
    }
    
    public void klausurHinzufuegen(Klausur klausur) {
        if (!zugeordneteKlausuren.contains(klausur)) {
            zugeordneteKlausuren.add(klausur);
        }
    }
    
    public List<Klausur> getZugeordneteKlausuren() {
        return new ArrayList<>(zugeordneteKlausuren);
    }
    
    public DozentenRolle getRolle() { return rolle; }
    public void setRolle(DozentenRolle rolle) { this.rolle = rolle; }
    
    @Override
    public String toString() {
        return super.toString() + " - " + rolle;
    }
}
