package gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.*;
import verwaltung.*;
import java.time.LocalDate;

/**
 * View zum Eintragen von Noten für bestehende Klausuren
 */
public class NotenView extends BorderPane {
    private final ErweiterteStudentenVerwaltung studentenVerwaltung;
    private final KlausurVerwaltung klausurVerwaltung;
    private final Runnable statistikUpdateCallback;

    private ComboBox<Student> studentComboBox;
    private ComboBox<Klausur> klausurComboBox;
    private TextField noteField;
    private TableView<Versuch> versucheTable;
    private TextArea statusArea;

    public NotenView(ErweiterteStudentenVerwaltung studentenVerwaltung, KlausurVerwaltung klausurVerwaltung,
                     Runnable statistikUpdateCallback) {
        this.studentenVerwaltung = studentenVerwaltung;
        this.klausurVerwaltung = klausurVerwaltung;
        this.statistikUpdateCallback = statistikUpdateCallback;

        setTop(createControlPanel());
        setCenter(createVersucheTable());
        setBottom(createStatusPanel());

        aktualisiereStudentenliste();
    }

    private VBox createControlPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("Noten eintragen");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox selection = new HBox(10);
        selection.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        studentComboBox = new ComboBox<>();
        studentComboBox.setPrefWidth(250);
        studentComboBox.setOnAction(e -> {
            aktualisiereKlausurliste();
            aktualisiereVersuchstabelle();
        });

        klausurComboBox = new ComboBox<>();
        klausurComboBox.setPrefWidth(250);

        noteField = new TextField();
        noteField.setPromptText("1.0 - 5.0");
        noteField.setPrefWidth(80);

        Button eintragenButton = new Button("Eintragen");
        eintragenButton.setOnAction(e -> versuchEintragen());

        selection.getChildren().addAll(
            new Label("Student:"), studentComboBox,
            new Label("Klausur:"), klausurComboBox,
            new Label("Note:"), noteField,
            eintragenButton
        );

        box.getChildren().addAll(title, selection);
        return box;
    }

    private TableView<Versuch> createVersucheTable() {
        versucheTable = new TableView<>();

        TableColumn<Versuch, String> klausurCol = new TableColumn<>("Klausur");
        klausurCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getKlausur().getTitel())
        );
        klausurCol.setPrefWidth(250);

        TableColumn<Versuch, Double> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(80);

        TableColumn<Versuch, LocalDate> datumCol = new TableColumn<>("Datum");
        datumCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("datum"));
        datumCol.setPrefWidth(120);

        TableColumn<Versuch, String> bewertungCol = new TableColumn<>("Bewertung");
        bewertungCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBewertung())
        );
        bewertungCol.setPrefWidth(150);

        versucheTable.getColumns().addAll(klausurCol, noteCol, datumCol, bewertungCol);
        return versucheTable;
    }

    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(10);
        statusPanel.setPadding(new Insets(10));
        statusPanel.setStyle("-fx-background-color: #f0f0f0;");

        Label label = new Label("Status:");
        label.setStyle("-fx-font-weight: bold;");

        statusArea = new TextArea();
        statusArea.setPrefRowCount(3);
        statusArea.setEditable(false);
        statusArea.setWrapText(true);

        statusPanel.getChildren().addAll(label, statusArea);
        return statusPanel;
    }

    private void aktualisiereStudentenliste() {
        ObservableList<Student> studenten = FXCollections.observableArrayList(
            studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)
        );
        studentComboBox.setItems(studenten);

        studentComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                    item.getMatrikelnummer() + " - " + item.getNachname() + ", " + item.getVorname());
            }
        });

        studentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                    item.getMatrikelnummer() + " - " + item.getNachname() + ", " + item.getVorname());
            }
        });
    }

    private void aktualisiereKlausurliste() {
        Student student = studentComboBox.getValue();
        if (student == null) {
            klausurComboBox.getItems().clear();
            return;
        }
        ObservableList<Klausur> klausuren = FXCollections.observableArrayList(
            student.getAngemeldeteKlausuren()
        );
        klausurComboBox.setItems(klausuren);

        klausurComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Klausur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + " - " + item.getTitel());
            }
        });

        klausurComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Klausur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + " - " + item.getTitel());
            }
        });
    }

    private void aktualisiereVersuchstabelle() {
        Student student = studentComboBox.getValue();
        if (student == null) {
            versucheTable.setItems(FXCollections.observableArrayList());
            return;
        }
        versucheTable.setItems(FXCollections.observableArrayList(student.getVersuche()));
    }

    private void versuchEintragen() {
        Student student = studentComboBox.getValue();
        Klausur klausur = klausurComboBox.getValue();
        String noteText = noteField.getText();

        if (student == null || klausur == null || noteText == null || noteText.isEmpty()) {
            showError("Bitte wählen Sie Student, Klausur und Note aus.");
            return;
        }

        try {
            double note = Double.parseDouble(noteText.replace(',', '.'));
            studentenVerwaltung.versuchEintragen(student.getMatrikelnummer(), klausur, note, LocalDate.now());
            statusArea.appendText(String.format("\n✓ Note %.1f für %s in %s eingetragen.", note,
                student.getNachname(), klausur.getTitel()));
            noteField.clear();
            aktualisiereVersuchstabelle();
            if (statistikUpdateCallback != null) {
                statistikUpdateCallback.run();
            }
        } catch (NumberFormatException ex) {
            showError("Ungültige Note. Bitte eine Zahl zwischen 1.0 und 5.0 eingeben.");
        } catch (Exception ex) {
            showError("Fehler beim Eintragen: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

