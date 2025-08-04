package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import gui.views.*;
import model.*;
import verwaltung.*;
import java.time.LocalDate;

public class KlausurverwaltungGUI extends Application {
    private ErweiterteStudentenVerwaltung studentenVerwaltung;
    private KlausurVerwaltung klausurVerwaltung;
    private BorderPane root;
    private Label statusLabel;
    
    @Override
    public void init() {
        // Initialisiere Verwaltungen
        studentenVerwaltung = new ErweiterteStudentenVerwaltung();
        klausurVerwaltung = new KlausurVerwaltung();
        ladeBeisspieldaten();
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Klausurverwaltungssystem v2.0");
        
        root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(createWelcomeView());
        root.setBottom(createStatusBar());
        
        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/gui/resources/style.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        updateStatus("Willkommen im Klausurverwaltungssystem!");
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Datei Menu
        Menu dateiMenu = new Menu("Datei");
        MenuItem exportItem = new MenuItem("Daten exportieren...");
        MenuItem importItem = new MenuItem("Daten importieren...");
        MenuItem beendenItem = new MenuItem("Beenden");
        beendenItem.setOnAction(e -> System.exit(0));
        dateiMenu.getItems().addAll(exportItem, importItem, new SeparatorMenuItem(), beendenItem);
        
        // Verwaltung Menu
        Menu verwaltungMenu = new Menu("Verwaltung");
        MenuItem studentenItem = new MenuItem("Studenten");
        studentenItem.setOnAction(e -> showStudentenVerwaltung());
        MenuItem klausurenItem = new MenuItem("Klausuren");
        klausurenItem.setOnAction(e -> showKlausurenVerwaltung());
        MenuItem anmeldungenItem = new MenuItem("Anmeldungen");
        anmeldungenItem.setOnAction(e -> showAnmeldungsVerwaltung());
        MenuItem notenItem = new MenuItem("Noten");
        notenItem.setOnAction(e -> showNotenVerwaltung());
        verwaltungMenu.getItems().addAll(studentenItem, klausurenItem, anmeldungenItem, notenItem);
        
        // Auswertung Menu
        Menu auswertungMenu = new Menu("Auswertung");
        MenuItem statistikItem = new MenuItem("Statistiken");
        statistikItem.setOnAction(e -> showStatistiken());
        MenuItem benachrichtigungenItem = new MenuItem("Benachrichtigungen");
        benachrichtigungenItem.setOnAction(e -> showBenachrichtigungen());
        auswertungMenu.getItems().addAll(statistikItem, benachrichtigungenItem);
        
        // Hilfe Menu
        Menu hilfeMenu = new Menu("Hilfe");
        MenuItem ueberItem = new MenuItem("Über...");
        ueberItem.setOnAction(e -> showAboutDialog());
        hilfeMenu.getItems().add(ueberItem);
        
        menuBar.getMenus().addAll(dateiMenu, verwaltungMenu, auswertungMenu, hilfeMenu);
        return menuBar;
    }
    
    private Node createWelcomeView() {
        VBox welcome = new VBox(20);
        welcome.setAlignment(javafx.geometry.Pos.CENTER);
        welcome.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Klausurverwaltungssystem");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Willkommen zur digitalen Prüfungsverwaltung");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: gray;");
        
        // Quick-Action Buttons
        HBox quickActions = new HBox(20);
        quickActions.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button studentButton = createQuickActionButton("Studenten\nverwalten", e -> showStudentenVerwaltung());
        Button klausurButton = createQuickActionButton("Klausuren\nverwalten", e -> showKlausurenVerwaltung());
        Button anmeldungButton = createQuickActionButton("Anmeldungen\nverwalten", e -> showAnmeldungsVerwaltung());
        Button notenButton = createQuickActionButton("Noten\neintragen", e -> showNotenVerwaltung());
        Button statistikButton = createQuickActionButton("Statistiken\nanzeigen", e -> showStatistiken());

        quickActions.getChildren().addAll(studentButton, klausurButton, anmeldungButton, notenButton, statistikButton);
        
        // Aktuelle Statistiken
        VBox stats = new VBox(10);
        stats.setAlignment(javafx.geometry.Pos.CENTER);
        stats.setPadding(new Insets(30, 0, 0, 0));
        
        int studentenAnzahl = studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME).size();
        int klausurenAnzahl = klausurVerwaltung.getAlleSortiert().size();
        int kommendeKlausuren = klausurVerwaltung.getKommendeKlausuren().size();
        
        Label statsLabel = new Label(String.format(
            "Aktuell: %d Studenten | %d Klausuren | %d kommende Prüfungen",
            studentenAnzahl, klausurenAnzahl, kommendeKlausuren
        ));
        statsLabel.setStyle("-fx-font-size: 14px;");
        
        stats.getChildren().add(statsLabel);
        
        welcome.getChildren().addAll(titleLabel, subtitleLabel, quickActions, stats);
        return welcome;
    }
    
    private Button createQuickActionButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button();
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        textLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        button.setGraphic(textLabel);
        button.setPrefSize(150, 100);
        button.setOnAction(handler);
        button.getStyleClass().add("quick-action-button");
        
        return button;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        
        statusLabel = new Label("Bereit");
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    private void showStudentenVerwaltung() {
        StudentenView view = new StudentenView(studentenVerwaltung);
        root.setCenter(view);
        updateStatus("Studentenverwaltung geöffnet");
    }
    
    private void showKlausurenVerwaltung() {
        KlausurenView view = new KlausurenView(klausurVerwaltung);
        root.setCenter(view);
        updateStatus("Klausurverwaltung geöffnet");
    }
    
    private void showAnmeldungsVerwaltung() {
        AnmeldungsView view = new AnmeldungsView(studentenVerwaltung, klausurVerwaltung);
        root.setCenter(view);
        updateStatus("Anmeldungsverwaltung geöffnet");
    }

    private void showNotenVerwaltung() {
        NotenView view = new NotenView(studentenVerwaltung, klausurVerwaltung, this::refreshStatistikenWennOffen);
        root.setCenter(view);
        updateStatus("Notenverwaltung geöffnet");
    }
    
    private void showStatistiken() {
        StatistikView view = new StatistikView(studentenVerwaltung, klausurVerwaltung);
        root.setCenter(view);
        updateStatus("Statistiken geöffnet");
    }
    
    private void showBenachrichtigungen() {
        BenachrichtigungsView view = new BenachrichtigungsView(studentenVerwaltung, klausurVerwaltung);
        root.setCenter(view);
        updateStatus("Benachrichtigungen geöffnet");
    }

    private void refreshStatistikenWennOffen() {
        if (root.getCenter() instanceof StatistikView) {
            ((StatistikView) root.getCenter()).refresh();
        }
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Über Klausurverwaltungssystem");
        alert.setHeaderText("Klausurverwaltungssystem v2.0");
        alert.setContentText(
            "Entwickelt für die Verwaltung von Studenten, Klausuren und Prüfungsanmeldungen.\n\n" +
            "Features:\n" +
            "• Studentenverwaltung mit Validierung\n" +
            "• Klausurplanung mit Konfliktprüfung\n" +
            "• Automatische Benachrichtigungen\n" +
            "• Umfangreiche Statistiken\n" +
            "• Persistente Datenspeicherung\n\n" +
            "© 2025 OOP-Projekt"
        );
        alert.showAndWait();
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void ladeBeisspieldaten() {
        try {
            // Beispiel-Studenten
            studentenVerwaltung.hinzufuegen(new Student("10001", "Max", "Mustermann", 
                "Informatik", LocalDate.of(2000, 5, 15)));
            studentenVerwaltung.hinzufuegen(new Student("10002", "Anna", "Schmidt", 
                "BWL", LocalDate.of(1999, 8, 22)));
            studentenVerwaltung.hinzufuegen(new Student("10003", "Tom", "Weber", 
                "Informatik", LocalDate.of(2001, 3, 10)));
            studentenVerwaltung.hinzufuegen(new Student("10004", "Lisa", "Mueller", 
                "Mathematik", LocalDate.of(2000, 11, 5)));
            studentenVerwaltung.hinzufuegen(new Student("10005", "Sarah", "Meyer", 
                "Physik", LocalDate.of(2000, 7, 18)));
            
            // Beispiel-Klausuren
            klausurVerwaltung.hinzufuegen(new Klausur("OOP2025", "Objektorientierte Programmierung", 
                "Informatik Grundlagen", java.time.LocalDateTime.of(2025, 7, 15, 10, 0), 
                "H1", 3, LocalDate.of(2025, 7, 1)));
            
            klausurVerwaltung.hinzufuegen(new Klausur("MATH1", "Mathematik I", 
                "Grundlagen", java.time.LocalDateTime.of(2025, 7, 20, 14, 0), 
                "A101", 3, LocalDate.of(2025, 7, 5)));
            
            klausurVerwaltung.hinzufuegen(new Klausur("BWL1", "Grundlagen BWL", 
                "Betriebswirtschaft", java.time.LocalDateTime.of(2025, 7, 18, 9, 0), 
                "B205", 3, LocalDate.of(2025, 7, 3)));
            
        } catch (Exception e) {
            // Daten existieren bereits
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}