package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Student erbt von Person und erweitert um studentenspezifische Eigenschaften
 */
public class Student extends Person implements Comparable<Student> {
    // Matrikelnummer-Format: 5-8 Ziffern
    private static final Pattern MATRIKELNUMMER_PATTERN = Pattern.compile("^\\d{5,8}$");
    private static final int MIN_MATRIKELNUMMER_LENGTH = 5;
    private static final int MAX_MATRIKELNUMMER_LENGTH = 8;
    
    private String matrikelnummer;
    private String studiengang;
    private List<Versuch> versuche;
    private List<Klausur> angemeldeteKlausuren;
    
    public Student(String matrikelnummer, String vorname, String nachname, String studiengang) {
        super(matrikelnummer, vorname, nachname, LocalDate.now()); // Temporary
        validateAndInitialize(matrikelnummer, studiengang);
    }
    
    public Student(String matrikelnummer, String vorname, String nachname, String studiengang, LocalDate geburtsdatum) {
        super(matrikelnummer, vorname, nachname, geburtsdatum);
        validateAndInitialize(matrikelnummer, studiengang);
    }
    
    private void validateAndInitialize(String matrikelnummer, String studiengang) {
        // Validiere Matrikelnummer
        if (!isValidMatrikelnummer(matrikelnummer)) {
            throw new IllegalArgumentException(
                "Ungültige Matrikelnummer! Muss " + MIN_MATRIKELNUMMER_LENGTH + 
                "-" + MAX_MATRIKELNUMMER_LENGTH + " Ziffern sein. Eingegeben: " + matrikelnummer
            );
        }
        
        // Validiere andere Felder
        if (getVorname() == null || getVorname().trim().isEmpty()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein!");
        }
        if (getNachname() == null || getNachname().trim().isEmpty()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein!");
        }
        if (studiengang == null || studiengang.trim().isEmpty()) {
            throw new IllegalArgumentException("Studiengang darf nicht leer sein!");
        }
        
        this.matrikelnummer = matrikelnummer;
        this.studiengang = studiengang.trim();
        this.versuche = new ArrayList<>();
        this.angemeldeteKlausuren = new ArrayList<>();
    }
    
    /**
     * Validiert eine Matrikelnummer
     */
    public static boolean isValidMatrikelnummer(String matrikelnummer) {
        if (matrikelnummer == null) return false;
        return MATRIKELNUMMER_PATTERN.matcher(matrikelnummer).matches();
    }
    
    /**
     * Generiert eine Beispiel-Matrikelnummer für Tests
     */
    public static String generateExampleMatrikelnummer() {
        return String.format("%05d", (int)(Math.random() * 100000));
    }
    
    // Getter/Setter
    public String getMatrikelnummer() { return matrikelnummer; }
    public String getStudiengang() { return studiengang; }
    public void setStudiengang(String studiengang) { 
        if (studiengang == null || studiengang.trim().isEmpty()) {
            throw new IllegalArgumentException("Studiengang darf nicht leer sein!");
        }
        this.studiengang = studiengang.trim(); 
    }
    
    // Versuch-Management
    public void addVersuch(Versuch versuch) {
        if (versuch != null) {
            this.versuche.add(versuch);
        }
    }
    
    public void removeVersuch(Versuch versuch) {
        this.versuche.remove(versuch);
    }
    
    public List<Versuch> getVersuche() {
        return new ArrayList<>(versuche); // Defensive copy
    }
    
    // Klausur-Anmeldung
    public void anmeldenZuKlausur(Klausur klausur) throws FristAbgelaufenException {
        if (klausur.istFristAbgelaufen()) {
            throw new FristAbgelaufenException("Anmeldefrist für " + klausur.getTitel() + " ist abgelaufen!");
        }
        if (!angemeldeteKlausuren.contains(klausur)) {
            angemeldeteKlausuren.add(klausur);
        }
    }
    
    public List<Klausur> getAngemeldeteKlausuren() {
        return new ArrayList<>(angemeldeteKlausuren);
    }
    
    /**
     * Zeigt den aktuellen Prüfungsstatus für alle Klausuren
     */
    public String zeigePruefungsstatus() {
        StringBuilder status = new StringBuilder();
        status.append("Prüfungsstatus für ").append(getVorname()).append(" ").append(getNachname()).append(":\n");
        
        for (Klausur klausur : angemeldeteKlausuren) {
            List<Versuch> klausurVersuche = getVersucheFuerKlausur(klausur);
            if (klausurVersuche.isEmpty()) {
                status.append("- ").append(klausur.getTitel()).append(": Angemeldet, noch nicht absolviert\n");
            } else {
                Versuch letzterVersuch = klausurVersuche.get(klausurVersuche.size() - 1);
                if (letzterVersuch.istBestanden()) {
                    status.append("- ").append(klausur.getTitel()).append(": BESTANDEN (Note: ")
                          .append(letzterVersuch.getNote()).append(")\n");
                } else {
                    status.append("- ").append(klausur.getTitel()).append(": Nicht bestanden (Versuch ")
                          .append(klausurVersuche.size()).append("/").append(klausur.getMaxVersuche()).append(")\n");
                }
            }
        }
        
        return status.toString();
    }
    
    /**
     * Berechnet den Notendurchschnitt aller bestandenen Prüfungen
     */
    public double berechneNotendurchschnitt() {
        List<Double> bestandeneNoten = new ArrayList<>();
        
        for (Versuch versuch : versuche) {
            if (versuch.istBestanden()) {
                bestandeneNoten.add(versuch.getNote());
            }
        }
        
        if (bestandeneNoten.isEmpty()) {
            return 0.0; // Keine bestandenen Prüfungen
        }
        
        double summe = bestandeneNoten.stream().mapToDouble(Double::doubleValue).sum();
        return summe / bestandeneNoten.size();
    }
    
    private List<Versuch> getVersucheFuerKlausur(Klausur klausur) {
        return versuche.stream()
                .filter(v -> v.getKlausur().equals(klausur))
                .toList();
    }
    
    // Überschreibe equals und hashCode für korrekte Duplikat-Erkennung
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return matrikelnummer.equals(student.matrikelnummer);
    }
    
    @Override
    public int hashCode() {
        return matrikelnummer.hashCode();
    }
    
    @Override
    public int compareTo(Student other) {
        return this.matrikelnummer.compareTo(other.matrikelnummer);
    }
    
    @Override
    public String toString() {
        return matrikelnummer + ": " + vorname + " " + nachname + " (" + studiengang + ")";
    }
}
