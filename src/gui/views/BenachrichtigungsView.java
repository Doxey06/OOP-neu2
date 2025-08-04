package gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.*;
import verwaltung.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BenachrichtigungsView extends BorderPane {
    private ErweiterteStudentenVerwaltung studentenVerwaltung;
    private KlausurVerwaltung klausurVerwaltung;
    private ComboBox<Student> studentComboBox;
    private ListView<Benachrichtigung> benachrichtigungsListe;
    private TextArea detailArea;
    private Label statistikLabel;
    private CheckBox nurUngeleseneCheckBox;
    
    public BenachrichtigungsView(ErweiterteStudentenVerwaltung studentenVerwaltung, KlausurVerwaltung klausurVerwaltung) {
        this.studentenVerwaltung = studentenVerwaltung;
        this.klausurVerwaltung = klausurVerwaltung;
        
        setTop(createControlPanel());
        setCenter(createMainContent());
        setBottom(createActionPanel());
        
        aktualisiereStudentenliste();
    }
    
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f8f9fa;");
        
        Label titleLabel = new Label("Benachrichtigungssystem");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label studentLabel = new Label("Student:");
        studentComboBox = new ComboBox<>();
        studentComboBox.setPrefWidth(300);
        studentComboBox.setOnAction(e -> aktualisiereBenachrichtigungen());
        
        nurUngeleseneCheckBox = new CheckBox("Nur ungelesene");
        nurUngeleseneCheckBox.setOnAction(e -> aktualisiereBenachrichtigungen());
        
        Button alleStudentenButton = new Button("Alle Studenten");
        alleStudentenButton.setOnAction(e -> {
            studentComboBox.setValue(null);
            aktualisiereBenachrichtigungen();
        });
        
        filterBox.getChildren().addAll(studentLabel, studentComboBox, nurUngeleseneCheckBox, alleStudentenButton);
        
        statistikLabel = new Label();
        statistikLabel.setStyle("-fx-font-style: italic;");
        
        controlPanel.getChildren().addAll(titleLabel, filterBox, statistikLabel);
        return controlPanel;
    }
    
    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        
        // Linke Seite: Benachrichtigungsliste
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(10));
        
        Label listLabel = new Label("Benachrichtigungen");
        listLabel.setStyle("-fx-font-weight: bold;");
        
        benachrichtigungsListe = new ListView<>();
        benachrichtigungsListe.setPrefHeight(400);
        benachrichtigungsListe.setCellFactory(lv -> new ListCell<Benachrichtigung>() {
            @Override
            protected void updateItem(Benachrichtigung item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cellContent = new VBox(2);
                    
                    HBox headerBox = new HBox(10);
                    Label typLabel = new Label(getTypIcon(item.getTyp()) + " " + item.getTyp().toString());
                    typLabel.setStyle("-fx-font-weight: bold;");
                    
                    Label zeitLabel = new Label(item.getZeitpunkt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    zeitLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
                    
                    headerBox.getChildren().addAll(typLabel, zeitLabel);
                    
                    Label nachrichtLabel = new Label(item.getNachricht());
                    nachrichtLabel.setWrapText(true);
                    nachrichtLabel.setMaxWidth(350);
                    
                    Label empfaengerLabel = new Label("An: " + item.getEmpfaenger().getVorname() + " " + item.getEmpfaenger().getNachname());
                    empfaengerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                    
                    cellContent.getChildren().addAll(headerBox, nachrichtLabel, empfaengerLabel);
                    
                    setGraphic(cellContent);
                    
                    if (!item.istGelesen()) {
                        setStyle("-fx-background-color: #e8f4fd;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        benachrichtigungsListe.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSel, newSel) -> {
                if (newSel != null) {
                    zeigeDetails(newSel);
                }
            }
        );
        
        leftBox.getChildren().addAll(listLabel, benachrichtigungsListe);
        
        // Rechte Seite: Details
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(10));
        
        Label detailLabel = new Label("Details");
        detailLabel.setStyle("-fx-font-weight: bold;");
        
        detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        detailArea.setPrefRowCount(20);
        
        rightBox.getChildren().addAll(detailLabel, detailArea);
        
        splitPane.getItems().addAll(leftBox, rightBox);
        splitPane.setDividerPositions(0.6);
        
        return splitPane;
    }
    
    private HBox createActionPanel() {
        HBox actionPanel = new HBox(10);
        actionPanel.setPadding(new Insets(10));
        actionPanel.setStyle("-fx-background-color: #f0f0f0;");
        
        Button erinnerungenButton = new Button("Automatische Erinnerungen erstellen");
        erinnerungenButton.setOnAction(e -> erstelleErinnerungen());
        
        Button alsGelesenButton = new Button("Als gelesen markieren");
        alsGelesenButton.setOnAction(e -> markiereAlsGelesen());
        
        Button alleAlsGelesenButton = new Button("Alle als gelesen");
        alleAlsGelesenButton.setOnAction(e -> markiereAlleAlsGelesen());
        
        Button loeschenButton = new Button("Löschen");
        loeschenButton.setOnAction(e -> loescheBenachrichtigung());
        
        actionPanel.getChildren().addAll(
            erinnerungenButton,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            alsGelesenButton,
            alleAlsGelesenButton,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            loeschenButton
        );
        
        return actionPanel;
    }
    
    private void aktualisiereStudentenliste() {
        ObservableList<Student> studenten = FXCollections.observableArrayList(
            studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)
        );
        studentComboBox.setItems(studenten);
        
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
                setText(empty || item == null ? "Alle Studenten" : 
                    item.getMatrikelnummer() + " - " + item.getNachname() + ", " + item.getVorname());
            }
        });
    }
    
    private void aktualisiereBenachrichtigungen() {
        ObservableList<Benachrichtigung> liste = FXCollections.observableArrayList();
        BenachrichtigungsVerwaltung verwaltung = studentenVerwaltung.getBenachrichtigungsVerwaltung();
        
        Student selectedStudent = studentComboBox.getValue();
        
        if (selectedStudent != null) {
            // Benachrichtigungen für einen bestimmten Studenten
            if (nurUngeleseneCheckBox.isSelected()) {
                liste.addAll(verwaltung.getUngelesene(selectedStudent));
            } else {
                liste.addAll(verwaltung.getBenachrichtigungenFuerStudent(selectedStudent));
            }
        } else {
            // Alle Benachrichtigungen
            for (Student student : studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)) {
                if (nurUngeleseneCheckBox.isSelected()) {
                    liste.addAll(verwaltung.getUngelesene(student));
                } else {
                    liste.addAll(verwaltung.getBenachrichtigungenFuerStudent(student));
                }
            }
        }
        
        benachrichtigungsListe.setItems(liste);
        
        // Statistik aktualisieren
        int ungelesen = (int) liste.stream().filter(b -> !b.istGelesen()).count();
        statistikLabel.setText(String.format("Gesamt: %d | Ungelesen: %d", liste.size(), ungelesen));
        
        detailArea.clear();
    }
    
    private void zeigeDetails(Benachrichtigung benachrichtigung) {
        StringBuilder details = new StringBuilder();
        
        details.append("=== BENACHRICHTIGUNGSDETAILS ===\n\n");
        details.append("Typ: ").append(benachrichtigung.getTyp()).append("\n");
        details.append("Empfänger: ").append(benachrichtigung.getEmpfaenger()).append("\n");
        details.append("Zeitpunkt: ").append(benachrichtigung.getZeitpunkt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))).append("\n");
        details.append("Status: ").append(benachrichtigung.istGelesen() ? "Gelesen" : "Ungelesen").append("\n\n");
        details.append("Nachricht:\n");
        details.append("-".repeat(40)).append("\n");
        details.append(benachrichtigung.getNachricht()).append("\n");
        details.append("-".repeat(40)).append("\n");
        
        detailArea.setText(details.toString());
        
        // Markiere als gelesen
        if (!benachrichtigung.istGelesen()) {
            benachrichtigung.markiereAlsGelesen();
            aktualisiereBenachrichtigungen();
        }
    }
    
    private void erstelleErinnerungen() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Erinnerungen erstellen");
        alert.setHeaderText("Automatische Erinnerungen");
        alert.setContentText("Möchten Sie für alle Studenten automatische Erinnerungen für anstehende Klausurfristen erstellen?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                List<Klausur> klausuren = klausurVerwaltung.getKommendeKlausuren();
                studentenVerwaltung.erstelleAutomatischeErinnerungen(klausuren);
                aktualisiereBenachrichtigungen();
                
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Erinnerungen erstellt");
                info.setHeaderText(null);
                info.setContentText("Automatische Erinnerungen wurden erfolgreich erstellt.");
                info.showAndWait();
            }
        });
    }
    
    private void markiereAlsGelesen() {
        Benachrichtigung selected = benachrichtigungsListe.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.istGelesen()) {
            selected.markiereAlsGelesen();
            aktualisiereBenachrichtigungen();
        }
    }
    
    private void markiereAlleAlsGelesen() {
        Student selectedStudent = studentComboBox.getValue();
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alle als gelesen markieren");
        alert.setHeaderText("Bestätigung");
        
        if (selectedStudent != null) {
            alert.setContentText("Alle Benachrichtigungen von " + selectedStudent.getVorname() + " " + selectedStudent.getNachname() + " als gelesen markieren?");
        } else {
            alert.setContentText("Wirklich ALLE Benachrichtigungen als gelesen markieren?");
        }
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                BenachrichtigungsVerwaltung verwaltung = studentenVerwaltung.getBenachrichtigungsVerwaltung();
                
                if (selectedStudent != null) {
                    verwaltung.alleAlsGelesenMarkieren(selectedStudent);
                } else {
                    for (Student student : studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)) {
                        verwaltung.alleAlsGelesenMarkieren(student);
                    }
                }
                
                aktualisiereBenachrichtigungen();
            }
        });
    }
    
    private void loescheBenachrichtigung() {
        Benachrichtigung selected = benachrichtigungsListe.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Benachrichtigung löschen");
            alert.setHeaderText("Bestätigung");
            alert.setContentText("Diese Benachrichtigung wirklich löschen?");
            
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    // Hier würde die Löschfunktion implementiert
                    // Momentan nur UI-Update
                    aktualisiereBenachrichtigungen();
                }
            });
        }
    }
    
    private String getTypIcon(Benachrichtigung.BenachrichtigungsTyp typ) {
        switch (typ) {
            case FRISTERINNERUNG:
                return "⏰";
            case KLAUSUR_ANMELDUNG:
                return "✍️";
            case NOTE_VERFUEGBAR:
                return "📊";
            case WARNUNG:
                return "⚠️";
            default:
                return "📧";
        }
    }
}
