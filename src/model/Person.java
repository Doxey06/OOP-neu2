package model;

import java.time.LocalDate;

/**
 * Basis-Klasse für alle Personen im System
 */
public abstract class Person {
    protected String id;
    protected String vorname;
    protected String nachname;
    protected LocalDate geburtsdatum;
    
    public Person(String id, String vorname, String nachname, LocalDate geburtsdatum) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.geburtsdatum = geburtsdatum;
    }
    
    // Getter
    public String getId() { return id; }
    public String getVorname() { return vorname; }
    public String getNachname() { return nachname; }
    public LocalDate getGeburtsdatum() { return geburtsdatum; }
    
    // Setter
    public void setVorname(String vorname) { this.vorname = vorname; }
    public void setNachname(String nachname) { this.nachname = nachname; }
    
    @Override
    public String toString() {
        return vorname + " " + nachname + " (ID: " + id + ")";
    }
}
