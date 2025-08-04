package gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Klausur;
import model.KlausurKonfliktException;
import verwaltung.KlausurVerwaltung;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class KlausurenView extends BorderPane {
    private KlausurVerwaltung verwaltung;
    private TableView<Klausur> tableView;
    private ObservableList<Klausur> klausurenListe;
    private CheckBox nurKommendeCheckBox;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public KlausurenView(KlausurVerwaltung verwaltung) {
        this.verwaltung = verwaltung;
        this.klausurenListe = FXCollections.observableArrayList();
        
        setTop(createToolBar());
        setCenter(createTableView());
        setBottom(createInfoBar());
        
        aktualisiereListe();
    }
    
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        
        Button neuButton = new Button("Neue Klausur");
        neuButton.setOnAction(e -> zeigeKlausurDialog(null));
        
        Button bearbeitenButton = new Button("Bearbeiten");
        bearbeitenButton.setOnAction(e -> {
            Klausur selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                zeigeKlausurDialog(selected);
            }
        });
        
        Button loeschenButton = new Button("Löschen");
        loeschenButton.setOnAction(e -> klausurLoeschen());
        
        nurKommendeCheckBox = new CheckBox("Nur kommende Klausuren");
        nurKommendeCheckBox.setOnAction(e -> aktualisiereListe());
        
        Button aktualisierenButton = new Button("Aktualisieren");
        aktualisierenButton.setOnAction(e -> aktualisiereListe());
        
        toolBar.getItems().addAll(
            neuButton, bearbeitenButton, loeschenButton,
            new Separator(),
            nurKommendeCheckBox,
            new Separator(),
            aktualisierenButton
        );
        
        return toolBar;
    }
    
    private TableView<Klausur> createTableView() {
        tableView = new TableView<>();
        
        TableColumn<Klausur, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<Klausur, String> titelCol = new TableColumn<>("Titel");
        titelCol.setCellValueFactory(new PropertyValueFactory<>("titel"));
        titelCol.setPrefWidth(250);
        
        TableColumn<Klausur, String> modulCol = new TableColumn<>("Modul");
        modulCol.setCellValueFactory(new PropertyValueFactory<>("modul"));
        modulCol.setPrefWidth(200);
        
        TableColumn<Klausur, LocalDateTime> datumCol = new TableColumn<>("Datum/Uhrzeit");
        datumCol.setCellValueFactory(new PropertyValueFactory<>("datum"));
        datumCol.setPrefWidth(150);
        datumCol.setCellFactory(col -> new TableCell<Klausur, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.format(DATE_TIME_FORMAT));
                    if (item.isBefore(LocalDateTime.now())) {
                        setStyle("-fx-text-fill: gray;");
                    } else if (item.isBefore(LocalDateTime.now().plusDays(7))) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Klausur, String> raumCol = new TableColumn<>("Raum");
        raumCol.setCellValueFactory(new PropertyValueFactory<>("raum"));
        raumCol.setPrefWidth(80);
        
        TableColumn<Klausur, LocalDate> fristCol = new TableColumn<>("Anmeldefrist");
        fristCol.setCellValueFactory(new PropertyValueFactory<>("anmeldefrist"));
        fristCol.setPrefWidth(120);
        fristCol.setCellFactory(col -> new TableCell<Klausur, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.format(DATE_FORMAT));
                    if (item.isBefore(LocalDate.now())) {
                        setText(item.format(DATE_FORMAT) + " (abgelaufen)");
                        setStyle("-fx-text-fill: red;");
                    } else if (item.isBefore(LocalDate.now().plusDays(3))) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Klausur, Integer> versucheCol = new TableColumn<>("Max. Versuche");
        versucheCol.setCellValueFactory(new PropertyValueFactory<>("maxVersuche"));
        versucheCol.setPrefWidth(100);
        
        tableView.getColumns().addAll(idCol, titelCol, modulCol, datumCol, raumCol, fristCol, versucheCol);
        tableView.setItems(klausurenListe);
        
        // Kontextmenü
        ContextMenu contextMenu = new ContextMenu();
        MenuItem teilnehmerItem = new MenuItem("Teilnehmer anzeigen");
        teilnehmerItem.setOnAction(e -> zeigeTeilnehmer());
        contextMenu.getItems().add(teilnehmerItem);
        tableView.setContextMenu(contextMenu);
        
        return tableView;
    }
    
    private HBox createInfoBar() {
        HBox infoBar = new HBox(20);
        infoBar.setPadding(new Insets(10));
        infoBar.setStyle("-fx-background-color: #f0f0f0;");
        
        Label gesamtLabel = new Label();
        Label kommendeLabel = new Label();
        Label heuteLabel = new Label();
        
        aktualisiereInfo(gesamtLabel, kommendeLabel, heuteLabel);
        
        infoBar.getChildren().addAll(
            new Label("Gesamt:"), gesamtLabel,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            new Label("Kommende:"), kommendeLabel,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            new Label("Diese Woche:"), heuteLabel
        );
        
        return infoBar;
    }
    
    private void aktualisiereInfo(Label gesamt, Label kommende, Label woche) {
        int gesamtAnzahl = verwaltung.getAlleSortiert().size();
        int kommendeAnzahl = verwaltung.getKommendeKlausuren().size();
        int wocheAnzahl = (int) verwaltung.getKommendeKlausuren().stream()
            .filter(k -> k.getDatum().isBefore(LocalDateTime.now().plusWeeks(1)))
            .count();
        
        gesamt.setText(String.valueOf(gesamtAnzahl));
        kommende.setText(String.valueOf(kommendeAnzahl));
        woche.setText(String.valueOf(wocheAnzahl));
        
        if (wocheAnzahl > 0) {
            woche.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        }
    }
    
    private void zeigeKlausurDialog(Klausur klausur) {
        Dialog<Klausur> dialog = new Dialog<>();
        dialog.setTitle(klausur == null ? "Neue Klausur" : "Klausur bearbeiten");
        dialog.setHeaderText(klausur == null ? "Neue Klausur anlegen" : "Klausurdaten bearbeiten");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField idField = new TextField(klausur != null ? klausur.getId() : "");
        idField.setPromptText("z.B. OOP2025");
        idField.setDisable(klausur != null);
        
        TextField titelField = new TextField(klausur != null ? klausur.getTitel() : "");
        TextField modulField = new TextField(klausur != null ? klausur.getModul() : "");
        TextField raumField = new TextField(klausur != null ? klausur.getRaum() : "");
        
        DatePicker datumPicker = new DatePicker(klausur != null ? klausur.getDatum().toLocalDate() : LocalDate.now().plusWeeks(4));
        Spinner<Integer> stundeSpinner = new Spinner<>(0, 23, klausur != null ? klausur.getDatum().getHour() : 10);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, klausur != null ? klausur.getDatum().getMinute() : 0, 15);
        
        HBox zeitBox = new HBox(5);
        zeitBox.getChildren().addAll(stundeSpinner, new Label(":"), minuteSpinner);
        
        DatePicker fristPicker = new DatePicker(klausur != null ? klausur.getAnmeldefrist() : LocalDate.now().plusWeeks(2));
        Spinner<Integer> versucheSpinner = new Spinner<>(1, 5, klausur != null ? klausur.getMaxVersuche() : 3);
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Titel:"), 0, 1);
        grid.add(titelField, 1, 1);
        grid.add(new Label("Modul:"), 0, 2);
        grid.add(modulField, 1, 2);
        grid.add(new Label("Raum:"), 0, 3);
        grid.add(raumField, 1, 3);
        grid.add(new Label("Datum:"), 0, 4);
        grid.add(datumPicker, 1, 4);
        grid.add(new Label("Uhrzeit:"), 0, 5);
        grid.add(zeitBox, 1, 5);
        grid.add(new Label("Anmeldefrist:"), 0, 6);
        grid.add(fristPicker, 1, 6);
        grid.add(new Label("Max. Versuche:"), 0, 7);
        grid.add(versucheSpinner, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType speichernButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(speichernButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == speichernButtonType) {
                try {
                    LocalDateTime dateTime = datumPicker.getValue().atTime(
                        stundeSpinner.getValue(), 
                        minuteSpinner.getValue()
                    );
                    
                    if (klausur == null) {
                        return new Klausur(
                            idField.getText().trim(),
                            titelField.getText().trim(),
                            modulField.getText().trim(),
                            dateTime,
                            raumField.getText().trim(),
                            versucheSpinner.getValue(),
                            fristPicker.getValue()
                        );
                    } else {
                        klausur.setDatum(dateTime);
                        klausur.setRaum(raumField.getText().trim());
                        klausur.setAnmeldefrist(fristPicker.getValue());
                        return klausur;
                    }
                } catch (Exception e) {
                    showError("Fehler", "Ungültige Eingabe: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<Klausur> result = dialog.showAndWait();
        result.ifPresent(k -> {
            try {
                if (klausur == null) {
                    verwaltung.hinzufuegen(k);
                    showInfo("Klausur hinzugefügt", "Klausur wurde erfolgreich angelegt.");
                } else {
                    showInfo("Klausur aktualisiert", "Änderungen wurden gespeichert.");
                }
                aktualisiereListe();
            } catch (KlausurKonfliktException e) {
                showError("Konflikt", e.getMessage());
            }
        });
    }
    
    private void klausurLoeschen() {
        Klausur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Klausur löschen");
        alert.setHeaderText("Klausur wirklich löschen?");
        alert.setContentText(selected.getTitel() + "\n\nAlle Anmeldungen werden ebenfalls gelöscht!");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (verwaltung.loeschen(selected.getId())) {
                aktualisiereListe();
                showInfo("Klausur gelöscht", "Klausur wurde erfolgreich entfernt.");
            }
        }
    }
    
    private void zeigeTeilnehmer() {
        Klausur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Teilnehmer");
        alert.setHeaderText("Angemeldete Studenten für: " + selected.getTitel());
        
        StringBuilder content = new StringBuilder();
        var teilnehmer = selected.getTeilnehmendeStudenten();
        if (teilnehmer.isEmpty()) {
            content.append("Keine Studenten angemeldet.");
        } else {
            content.append("Anzahl: ").append(teilnehmer.size()).append("\n\n");
            teilnehmer.forEach(s -> content.append("• ").append(s).append("\n"));
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void aktualisiereListe() {
        klausurenListe.clear();
        if (nurKommendeCheckBox.isSelected()) {
            klausurenListe.addAll(verwaltung.getKommendeKlausuren());
        } else {
            klausurenListe.addAll(verwaltung.getAlleSortiert());
        }
        
        // Info-Bar aktualisieren
        HBox infoBar = (HBox) getBottom();
        if (infoBar != null && infoBar.getChildren().size() >= 6) {
            Label gesamt = (Label) infoBar.getChildren().get(1);
            Label kommende = (Label) infoBar.getChildren().get(4);
            Label woche = (Label) infoBar.getChildren().get(7);
            aktualisiereInfo(gesamt, kommende, woche);
        }
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
