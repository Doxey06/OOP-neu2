package gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.*;
import verwaltung.*;
import java.util.List;
import java.util.Optional;

public class AnmeldungsView extends BorderPane {
    private ErweiterteStudentenVerwaltung studentenVerwaltung;
    private KlausurVerwaltung klausurVerwaltung;
    private ComboBox<Student> studentComboBox;
    private ListView<Klausur> verfuegbareKlausurenList;
    private ListView<Klausur> angemeldeteKlausurenList;
    private TextArea statusArea;
    
    public AnmeldungsView(ErweiterteStudentenVerwaltung studentenVerwaltung, KlausurVerwaltung klausurVerwaltung) {
        this.studentenVerwaltung = studentenVerwaltung;
        this.klausurVerwaltung = klausurVerwaltung;
        
        setTop(createControlPanel());
        setCenter(createMainContent());
        setBottom(createStatusPanel());
        
        aktualisiereStudentenliste();
    }
    
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f8f9fa;");
        
        Label titleLabel = new Label("Klausuranmeldungen verwalten");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox studentBox = new HBox(10);
        studentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label studentLabel = new Label("Student auswählen:");
        studentComboBox = new ComboBox<>();
        studentComboBox.setPrefWidth(300);
        studentComboBox.setOnAction(e -> aktualisiereKlausurListen());
        
        Button aktualisierenButton = new Button("Aktualisieren");
        aktualisierenButton.setOnAction(e -> {
            aktualisiereStudentenliste();
            aktualisiereKlausurListen();
        });
        
        studentBox.getChildren().addAll(studentLabel, studentComboBox, aktualisierenButton);
        
        controlPanel.getChildren().addAll(titleLabel, studentBox);
        return controlPanel;
    }
    
    private HBox createMainContent() {
        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        
        // Verfügbare Klausuren
        VBox verfuegbarBox = new VBox(10);
        Label verfuegbarLabel = new Label("Verfügbare Klausuren");
        verfuegbarLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        verfuegbareKlausurenList = new ListView<>();
        verfuegbareKlausurenList.setPrefSize(400, 300);
        verfuegbareKlausurenList.setCellFactory(lv -> new ListCell<Klausur>() {
            @Override
            protected void updateItem(Klausur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%s - %s\n  Termin: %s | Raum: %s\n  Anmeldefrist: %s",
                        item.getId(), item.getTitel(),
                        item.getDatum().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                        item.getRaum(),
                        item.getAnmeldefrist().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    ));
                    
                    if (item.istFristAbgelaufen()) {
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                        setDisable(true);
                    }
                }
            }
        });
        
        Button anmeldenButton = new Button("→ Anmelden");
        anmeldenButton.setOnAction(e -> studentAnmelden());
        anmeldenButton.setPrefWidth(400);
        
        verfuegbarBox.getChildren().addAll(verfuegbarLabel, verfuegbareKlausurenList, anmeldenButton);
        
        // Angemeldete Klausuren
        VBox angemeldetBox = new VBox(10);
        Label angemeldetLabel = new Label("Angemeldete Klausuren");
        angemeldetLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        angemeldeteKlausurenList = new ListView<>();
        angemeldeteKlausurenList.setPrefSize(400, 300);
        angemeldeteKlausurenList.setCellFactory(lv -> new ListCell<Klausur>() {
            @Override
            protected void updateItem(Klausur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Student selected = studentComboBox.getValue();
                    if (selected != null) {
                        List<Versuch> versuche = selected.getVersuche().stream()
                            .filter(v -> v.getKlausur().equals(item))
                            .toList();
                        
                        String status = versuche.isEmpty() ? "Angemeldet" : 
                            versuche.stream().anyMatch(v -> v.istBestanden()) ? "BESTANDEN" : "Nicht bestanden";
                        
                        setText(String.format("%s - %s\n  Status: %s\n  Termin: %s",
                            item.getId(), item.getTitel(),
                            status,
                            item.getDatum().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        ));
                        
                        if (status.equals("BESTANDEN")) {
                            setStyle("-fx-text-fill: green;");
                        } else if (status.equals("Nicht bestanden")) {
                            setStyle("-fx-text-fill: orange;");
                        }
                    }
                }
            }
        });
        
        Button abmeldenButton = new Button("← Abmelden");
        abmeldenButton.setOnAction(e -> studentAbmelden());
        abmeldenButton.setPrefWidth(400);
        
        angemeldetBox.getChildren().addAll(angemeldetLabel, angemeldeteKlausurenList, abmeldenButton);
        
        content.getChildren().addAll(verfuegbarBox, angemeldetBox);
        return content;
    }
    
    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(10);
        statusPanel.setPadding(new Insets(10));
        statusPanel.setStyle("-fx-background-color: #f0f0f0;");
        
        Label statusLabel = new Label("Status und Meldungen:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        statusArea = new TextArea();
        statusArea.setPrefRowCount(3);
        statusArea.setEditable(false);
        statusArea.setWrapText(true);
        
        statusPanel.getChildren().addAll(statusLabel, statusArea);
        return statusPanel;
    }
    
    private void aktualisiereStudentenliste() {
        ObservableList<Student> studenten = FXCollections.observableArrayList(
            studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)
        );
        studentComboBox.setItems(studenten);
        
        // Custom Cell Factory für bessere Anzeige
        studentComboBox.setCellFactory(lv -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : 
                    item.getMatrikelnummer() + " - " + item.getNachname() + ", " + item.getVorname());
            }
        });
        
        studentComboBox.setButtonCell(new ListCell<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : 
                    item.getMatrikelnummer() + " - " + item.getNachname() + ", " + item.getVorname());
            }
        });
    }
    
    private void aktualisiereKlausurListen() {
        Student selected = studentComboBox.getValue();
        if (selected == null) {
            verfuegbareKlausurenList.getItems().clear();
            angemeldeteKlausurenList.getItems().clear();
            statusArea.setText("Bitte wählen Sie einen Studenten aus.");
            return;
        }
        
        // Angemeldete Klausuren
        ObservableList<Klausur> angemeldet = FXCollections.observableArrayList(
            selected.getAngemeldeteKlausuren()
        );
        angemeldeteKlausurenList.setItems(angemeldet);
        
        // Verfügbare Klausuren (noch nicht angemeldet)
        List<Klausur> alleKlausuren = klausurVerwaltung.getKommendeKlausuren();
        ObservableList<Klausur> verfuegbar = FXCollections.observableArrayList(
            alleKlausuren.stream()
                .filter(k -> !angemeldet.contains(k))
                .toList()
        );
        verfuegbareKlausurenList.setItems(verfuegbar);
        
        // Status aktualisieren
        statusArea.setText(String.format("Student: %s\nAngemeldete Klausuren: %d\nVerfügbare Klausuren: %d",
            selected.toString(), angemeldet.size(), verfuegbar.size()));
    }
    
    private void studentAnmelden() {
        Student student = studentComboBox.getValue();
        Klausur klausur = verfuegbareKlausurenList.getSelectionModel().getSelectedItem();
        
        if (student == null || klausur == null) {
            showError("Fehler", "Bitte wählen Sie einen Studenten und eine Klausur aus.");
            return;
        }
        
        try {
            studentenVerwaltung.anmeldenZuKlausur(student.getMatrikelnummer(), klausur);
            aktualisiereKlausurListen();
            statusArea.appendText(String.format("\n✓ %s erfolgreich zu %s angemeldet.", 
                student.getVorname(), klausur.getTitel()));
        } catch (FristAbgelaufenException e) {
            showError("Anmeldefrist abgelaufen", 
                "Die Anmeldefrist für diese Klausur ist bereits abgelaufen.");
        } catch (KlausurKonfliktException e) {
            showError("Terminkonflikt", e.getMessage());
        } catch (Exception e) {
            showError("Fehler", "Anmeldung fehlgeschlagen: " + e.getMessage());
        }
    }
    
    private void studentAbmelden() {
        Student student = studentComboBox.getValue();
        Klausur klausur = angemeldeteKlausurenList.getSelectionModel().getSelectedItem();
        
        if (student == null || klausur == null) {
            showError("Fehler", "Bitte wählen Sie einen Studenten und eine Klausur aus.");
            return;
        }
        
        // Prüfe ob bereits Versuche existieren
        boolean hatVersuche = student.getVersuche().stream()
            .anyMatch(v -> v.getKlausur().equals(klausur));
        
        if (hatVersuche) {
            showError("Abmeldung nicht möglich", 
                "Student hat bereits an dieser Klausur teilgenommen.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Abmeldung bestätigen");
        confirm.setHeaderText("Wirklich abmelden?");
        confirm.setContentText(String.format("%s von %s abmelden?", 
            student.getVorname() + " " + student.getNachname(), klausur.getTitel()));
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Hier würde die Abmeldung implementiert
                // Momentan nur UI-Update
                aktualisiereKlausurListen();
                statusArea.appendText(String.format("\n✓ %s von %s abgemeldet.", 
                    student.getVorname(), klausur.getTitel()));
            }
        });
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}