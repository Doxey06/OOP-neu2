package gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Student;
import verwaltung.ErweiterteStudentenVerwaltung;
import verwaltung.ErweiterteStudentenVerwaltung.DuplikatException;
import verwaltung.ErweiterteStudentenVerwaltung.ValidationResult;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

public class StudentenView extends BorderPane {
    private ErweiterteStudentenVerwaltung verwaltung;
    private TableView<Student> tableView;
    private ObservableList<Student> studentenListe;
    private TextField suchfeld;
    private ComboBox<String> suchkriteriumBox;
    private Label statistikLabel;
    private final Consumer<Student> notenHandler;

    public StudentenView(ErweiterteStudentenVerwaltung verwaltung, Consumer<Student> notenHandler) {
        this.verwaltung = verwaltung;
        this.notenHandler = notenHandler;
        this.studentenListe = FXCollections.observableArrayList();

        setTop(createToolBar());
        setCenter(createTableView());
        setBottom(createStatistikBar());

        aktualisiereListe();
    }
    
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        
        // Buttons
        Button neuButton = new Button("Neuer Student");
        neuButton.setOnAction(e -> zeigeStudentDialog(null));
        
        Button bearbeitenButton = new Button("Bearbeiten");
        bearbeitenButton.setOnAction(e -> {
            Student selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                zeigeStudentDialog(selected);
            }
        });
        
        Button loeschenButton = new Button("Löschen");
        loeschenButton.setOnAction(e -> studentLoeschen());

        Button notenButton = new Button("Note eintragen");
        notenButton.setOnAction(e -> {
            Student selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && notenHandler != null) {
                notenHandler.accept(selected);
            }
        });
        
        // Suche
        suchfeld = new TextField();
        suchfeld.setPromptText("Suchen...");
        suchfeld.textProperty().addListener((obs, alt, neu) -> sucheStudenten());
        
        suchkriteriumBox = new ComboBox<>();
        suchkriteriumBox.getItems().addAll("Name", "Matrikelnummer", "Studiengang");
        suchkriteriumBox.setValue("Name");
        suchkriteriumBox.setOnAction(e -> sucheStudenten());
        
        Button aktualisierenButton = new Button("Aktualisieren");
        aktualisierenButton.setOnAction(e -> aktualisiereListe());
        
        toolBar.getItems().addAll(
            neuButton, bearbeitenButton, loeschenButton, notenButton,
            new Separator(),
            new Label("Suche:"), suchkriteriumBox, suchfeld,
            new Separator(),
            aktualisierenButton
        );
        
        return toolBar;
    }
    
    private TableView<Student> createTableView() {
        tableView = new TableView<>();
        
        // Spalten definieren
        TableColumn<Student, String> matrikelCol = new TableColumn<>("Matrikelnummer");
        matrikelCol.setCellValueFactory(new PropertyValueFactory<>("matrikelnummer"));
        matrikelCol.setPrefWidth(120);
        
        TableColumn<Student, String> vornameCol = new TableColumn<>("Vorname");
        vornameCol.setCellValueFactory(new PropertyValueFactory<>("vorname"));
        vornameCol.setPrefWidth(150);
        
        TableColumn<Student, String> nachnameCol = new TableColumn<>("Nachname");
        nachnameCol.setCellValueFactory(new PropertyValueFactory<>("nachname"));
        nachnameCol.setPrefWidth(150);
        
        TableColumn<Student, String> studiengangCol = new TableColumn<>("Studiengang");
        studiengangCol.setCellValueFactory(new PropertyValueFactory<>("studiengang"));
        studiengangCol.setPrefWidth(200);
        
        TableColumn<Student, LocalDate> geburtsdatumCol = new TableColumn<>("Geburtsdatum");
        geburtsdatumCol.setCellValueFactory(new PropertyValueFactory<>("geburtsdatum"));
        geburtsdatumCol.setPrefWidth(120);
        
        TableColumn<Student, Double> durchschnittCol = new TableColumn<>("Notendurchschnitt");
        durchschnittCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().berechneNotendurchschnitt()).asObject()
        );
        durchschnittCol.setPrefWidth(130);
        durchschnittCol.setCellFactory(col -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("-");
                } else {
                    setText(String.format("%.2f", item));
                    if (item > 3.0) {
                        setStyle("-fx-text-fill: red;");
                    } else if (item < 2.0) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        tableView.getColumns().addAll(matrikelCol, vornameCol, nachnameCol, studiengangCol, geburtsdatumCol, durchschnittCol);
        tableView.setItems(studentenListe);
        
        // Doppelklick zum Bearbeiten
        tableView.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    zeigeStudentDialog(row.getItem());
                }
            });
            return row;
        });
        
        return tableView;
    }
    
    private HBox createStatistikBar() {
        HBox statistikBar = new HBox(10);
        statistikBar.setPadding(new Insets(10));
        statistikBar.setStyle("-fx-background-color: #f0f0f0;");
        
        statistikLabel = new Label();
        aktualisiereStatistik();
        
        statistikBar.getChildren().add(statistikLabel);
        return statistikBar;
    }
    
    private void zeigeStudentDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Neuer Student" : "Student bearbeiten");
        dialog.setHeaderText(student == null ? "Neuen Studenten anlegen" : "Studentendaten bearbeiten");
        
        // Form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField matrikelField = new TextField(student != null ? student.getMatrikelnummer() : "");
        matrikelField.setPromptText("5-8 Ziffern");
        matrikelField.setDisable(student != null); // Matrikelnummer nicht änderbar
        
        TextField vornameField = new TextField(student != null ? student.getVorname() : "");
        TextField nachnameField = new TextField(student != null ? student.getNachname() : "");
        TextField studiengangField = new TextField(student != null ? student.getStudiengang() : "");
        DatePicker geburtsdatumPicker = new DatePicker(student != null ? student.getGeburtsdatum() : null);
        
        Label validierungLabel = new Label();
        validierungLabel.setStyle("-fx-text-fill: red;");
        
        // Validierung bei Eingabe
        matrikelField.textProperty().addListener((obs, alt, neu) -> {
            if (student == null && !neu.isEmpty()) {
                ValidationResult result = verwaltung.validiereMatrikelnummer(neu);
                if (!result.isValid) {
                    validierungLabel.setText(result.message);
                    matrikelField.setStyle("-fx-border-color: red;");
                } else {
                    validierungLabel.setText("");
                    matrikelField.setStyle("");
                }
            }
        });
        
        grid.add(new Label("Matrikelnummer:"), 0, 0);
        grid.add(matrikelField, 1, 0);
        grid.add(new Label("Vorname:"), 0, 1);
        grid.add(vornameField, 1, 1);
        grid.add(new Label("Nachname:"), 0, 2);
        grid.add(nachnameField, 1, 2);
        grid.add(new Label("Studiengang:"), 0, 3);
        grid.add(studiengangField, 1, 3);
        grid.add(new Label("Geburtsdatum:"), 0, 4);
        grid.add(geburtsdatumPicker, 1, 4);
        grid.add(validierungLabel, 0, 5, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Buttons
        ButtonType speichernButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(speichernButtonType, ButtonType.CANCEL);
        
        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == speichernButtonType) {
                try {
                    if (student == null) {
                        // Neuer Student
                        return new Student(
                            matrikelField.getText().trim(),
                            vornameField.getText().trim(),
                            nachnameField.getText().trim(),
                            studiengangField.getText().trim(),
                            geburtsdatumPicker.getValue()
                        );
                    } else {
                        // Bestehender Student bearbeiten
                        student.setVorname(vornameField.getText().trim());
                        student.setNachname(nachnameField.getText().trim());
                        student.setStudiengang(studiengangField.getText().trim());
                        return student;
                    }
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Ungültige Eingabe");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
        
        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (student == null) {
                // Neuen Studenten hinzufügen
                try {
                    verwaltung.hinzufuegenMitValidierung(s);
                    aktualisiereListe();
                    showInfo("Student hinzugefügt", "Student wurde erfolgreich angelegt.");
                } catch (DuplikatException e) {
                    showError("Fehler", e.getMessage());
                }
            } else {
                // Student wurde bearbeitet
                aktualisiereListe();
                showInfo("Student aktualisiert", "Änderungen wurden gespeichert.");
            }
        });
    }
    
    private void studentLoeschen() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Student löschen");
        alert.setHeaderText("Student wirklich löschen?");
        alert.setContentText(selected.toString() + "\n\nDieser Vorgang kann nicht rückgängig gemacht werden!");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (verwaltung.loeschen(selected.getMatrikelnummer())) {
                aktualisiereListe();
                showInfo("Student gelöscht", "Student wurde erfolgreich entfernt.");
            }
        }
    }
    
    private void sucheStudenten() {
        String suchtext = suchfeld.getText().trim();
        if (suchtext.isEmpty()) {
            aktualisiereListe();
            return;
        }
        
        studentenListe.clear();
        String kriterium = suchkriteriumBox.getValue();
        
        switch (kriterium) {
            case "Name":
                studentenListe.addAll(verwaltung.suchenNachName(suchtext));
                break;
            case "Matrikelnummer":
                Student student = verwaltung.findeNachMatrikelnummer(suchtext);
                if (student != null) {
                    studentenListe.add(student);
                }
                break;
            case "Studiengang":
                studentenListe.addAll(verwaltung.suchenNachStudiengang(suchtext));
                break;
        }
        
        aktualisiereStatistik();
    }
    
    private void aktualisiereListe() {
        studentenListe.clear();
        studentenListe.addAll(verwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME));
        aktualisiereStatistik();
    }
    
    private void aktualisiereStatistik() {
        var stats = verwaltung.getMatrikelnummerStatistik();
        statistikLabel.setText(String.format(
            "Studenten: %d | Matrikelnummern: %s - %s | Durchschnittliche Länge: %.1f",
            studentenListe.size(),
            stats.niedrigste.isEmpty() ? "keine" : stats.niedrigste,
            stats.hoechste.isEmpty() ? "keine" : stats.hoechste,
            stats.durchschnittlicheLaenge
        ));
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}