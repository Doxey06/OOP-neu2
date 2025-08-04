package gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.*;
import verwaltung.*;
import java.util.*;
import java.util.stream.Collectors;

public class StatistikView extends BorderPane {
    private ErweiterteStudentenVerwaltung studentenVerwaltung;
    private KlausurVerwaltung klausurVerwaltung;
    private TabPane tabPane;
    
    public StatistikView(ErweiterteStudentenVerwaltung studentenVerwaltung, KlausurVerwaltung klausurVerwaltung) {
        this.studentenVerwaltung = studentenVerwaltung;
        this.klausurVerwaltung = klausurVerwaltung;
        
        Label titleLabel = new Label("Statistiken und Auswertungen");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10;");
        setTop(titleLabel);
        
        tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createUebersichtTab(),
            createNotenverteilungTab(),
            createStudiengangTab(),
            createKlausurstatistikTab(),
            createLeistungsTab()
        );

        setCenter(tabPane);
    }

    /**
     * Aktualisiert alle Statistik-Tabs nach Datenänderungen
     */
    public void refresh() {
        tabPane.getTabs().setAll(
            createUebersichtTab(),
            createNotenverteilungTab(),
            createStudiengangTab(),
            createKlausurstatistikTab(),
            createLeistungsTab()
        );
    }
    
    private Tab createUebersichtTab() {
        Tab tab = new Tab("Übersicht");
        tab.setClosable(false);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        
        // Statistik-Boxen
        VBox studentenBox = createStatBox("Studenten", 
            String.valueOf(studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME).size()),
            "Gesamt eingeschrieben");
        
        VBox klausurenBox = createStatBox("Klausuren",
            String.valueOf(klausurVerwaltung.getAlleSortiert().size()),
            "Im System");
        
        VBox kommendeBox = createStatBox("Anstehend",
            String.valueOf(klausurVerwaltung.getKommendeKlausuren().size()),
            "Kommende Klausuren");
        
        // Berechne Durchschnittsnote
        double avgNote = studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME).stream()
            .mapToDouble(Student::berechneNotendurchschnitt)
            .filter(note -> note > 0)
            .average()
            .orElse(0.0);
        
        VBox durchschnittBox = createStatBox("Ø-Note",
            String.format("%.2f", avgNote),
            "Gesamtdurchschnitt");
        
        grid.add(studentenBox, 0, 0);
        grid.add(klausurenBox, 1, 0);
        grid.add(kommendeBox, 2, 0);
        grid.add(durchschnittBox, 3, 0);
        
        // Schnellübersicht Liste
        VBox quickStats = new VBox(10);
        quickStats.setPadding(new Insets(20, 0, 0, 0));
        
        Label quickLabel = new Label("Schnellübersicht");
        quickLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ListView<String> quickList = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();
        
        // Studiengänge mit Anzahl
        Map<String, Long> studiengangCount = studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.STUDIENGANG)
            .stream()
            .collect(Collectors.groupingBy(Student::getStudiengang, Collectors.counting()));
        
        items.add("=== Studiengänge ===");
        studiengangCount.forEach((studiengang, count) -> 
            items.add(String.format("%s: %d Studenten", studiengang, count))
        );
        
        items.add("");
        items.add("=== Klausuren diese Woche ===");
        klausurVerwaltung.getKommendeKlausuren().stream()
            .filter(k -> k.getDatum().isBefore(java.time.LocalDateTime.now().plusWeeks(1)))
            .forEach(k -> items.add(k.getTitel() + " - " + k.getDatum().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM. HH:mm"))));
        
        quickList.setItems(items);
        quickList.setPrefHeight(300);
        
        quickStats.getChildren().addAll(quickLabel, quickList);
        
        VBox content = new VBox(20);
        content.getChildren().addAll(grid, quickStats);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }
    
    private Tab createNotenverteilungTab() {
        Tab tab = new Tab("Notenverteilung");
        tab.setClosable(false);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Notenverteilung Histogram
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Notenbereiche");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Anzahl Studenten");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Notenverteilung aller Studenten");
        
        // Daten sammeln
        Map<String, Integer> notenVerteilung = new TreeMap<>();
        notenVerteilung.put("1.0-1.5", 0);
        notenVerteilung.put("1.6-2.0", 0);
        notenVerteilung.put("2.1-2.5", 0);
        notenVerteilung.put("2.6-3.0", 0);
        notenVerteilung.put("3.1-3.5", 0);
        notenVerteilung.put("3.6-4.0", 0);
        notenVerteilung.put("> 4.0", 0);
        notenVerteilung.put("Keine Note", 0);
        
        for (Student student : studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)) {
            double durchschnitt = student.berechneNotendurchschnitt();
            if (durchschnitt == 0) {
                notenVerteilung.merge("Keine Note", 1, Integer::sum);
            } else if (durchschnitt <= 1.5) {
                notenVerteilung.merge("1.0-1.5", 1, Integer::sum);
            } else if (durchschnitt <= 2.0) {
                notenVerteilung.merge("1.6-2.0", 1, Integer::sum);
            } else if (durchschnitt <= 2.5) {
                notenVerteilung.merge("2.1-2.5", 1, Integer::sum);
            } else if (durchschnitt <= 3.0) {
                notenVerteilung.merge("2.6-3.0", 1, Integer::sum);
            } else if (durchschnitt <= 3.5) {
                notenVerteilung.merge("3.1-3.5", 1, Integer::sum);
            } else if (durchschnitt <= 4.0) {
                notenVerteilung.merge("3.6-4.0", 1, Integer::sum);
            } else {
                notenVerteilung.merge("> 4.0", 1, Integer::sum);
            }
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Anzahl Studenten");
        notenVerteilung.forEach((bereich, anzahl) -> 
            series.getData().add(new XYChart.Data<>(bereich, anzahl))
        );
        
        barChart.getData().add(series);
        barChart.setPrefHeight(400);
        
        // Statistik-Text
        TextArea statsText = new TextArea();
        statsText.setEditable(false);
        statsText.setPrefRowCount(8);
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== DETAILLIERTE NOTENSTATISTIK ===\n\n");
        
        List<Student> mitNoten = studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)
            .stream()
            .filter(s -> s.berechneNotendurchschnitt() > 0)
            .sorted(Comparator.comparing(Student::berechneNotendurchschnitt))
            .collect(Collectors.toList());
        
        if (!mitNoten.isEmpty()) {
            stats.append(String.format("Beste Note: %.2f (%s)\n", 
                mitNoten.get(0).berechneNotendurchschnitt(),
                mitNoten.get(0).getNachname() + ", " + mitNoten.get(0).getVorname()));
            
            stats.append(String.format("Schlechteste Note: %.2f (%s)\n",
                mitNoten.get(mitNoten.size()-1).berechneNotendurchschnitt(),
                mitNoten.get(mitNoten.size()-1).getNachname() + ", " + mitNoten.get(mitNoten.size()-1).getVorname()));
            
            double median = mitNoten.size() % 2 == 0 ?
                (mitNoten.get(mitNoten.size()/2-1).berechneNotendurchschnitt() + 
                 mitNoten.get(mitNoten.size()/2).berechneNotendurchschnitt()) / 2 :
                mitNoten.get(mitNoten.size()/2).berechneNotendurchschnitt();
            
            stats.append(String.format("Median: %.2f\n", median));
            stats.append(String.format("Studenten mit Noten: %d von %d\n", 
                mitNoten.size(), 
                studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME).size()));
        }
        
        statsText.setText(stats.toString());
        
        content.getChildren().addAll(barChart, statsText);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }
    
    private Tab createStudiengangTab() {
        Tab tab = new Tab("Studiengänge");
        tab.setClosable(false);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Pie Chart für Studiengangverteilung
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Verteilung nach Studiengängen");
        
        Map<String, Long> studiengangCount = studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.STUDIENGANG)
            .stream()
            .collect(Collectors.groupingBy(Student::getStudiengang, Collectors.counting()));
        
        studiengangCount.forEach((studiengang, count) -> {
            PieChart.Data slice = new PieChart.Data(studiengang + " (" + count + ")", count);
            pieChart.getData().add(slice);
        });
        
        pieChart.setLabelLineLength(10);
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setPrefHeight(400);
        
        // Tabelle mit Details pro Studiengang
        TableView<StudiengangStatistik> table = new TableView<>();
        
        TableColumn<StudiengangStatistik, String> studiengangCol = new TableColumn<>("Studiengang");
        studiengangCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().studiengang));
        studiengangCol.setPrefWidth(200);
        
        TableColumn<StudiengangStatistik, Integer> anzahlCol = new TableColumn<>("Anzahl");
        anzahlCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().anzahl).asObject());
        anzahlCol.setPrefWidth(100);
        
        TableColumn<StudiengangStatistik, Double> durchschnittCol = new TableColumn<>("Ø-Note");
        durchschnittCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().durchschnitt).asObject());
        durchschnittCol.setPrefWidth(100);
        durchschnittCol.setCellFactory(col -> new TableCell<StudiengangStatistik, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("-");
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        TableColumn<StudiengangStatistik, Integer> mitNotenCol = new TableColumn<>("Mit Noten");
        mitNotenCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().mitNoten).asObject());
        mitNotenCol.setPrefWidth(100);
        
        table.getColumns().addAll(studiengangCol, anzahlCol, durchschnittCol, mitNotenCol);
        
        // Daten für Tabelle sammeln
        ObservableList<StudiengangStatistik> tableData = FXCollections.observableArrayList();
        
        studiengangCount.forEach((studiengang, count) -> {
            List<Student> studiengangStudenten = studentenVerwaltung.suchenNachStudiengang(studiengang);
            double avgNote = studiengangStudenten.stream()
                .mapToDouble(Student::berechneNotendurchschnitt)
                .filter(note -> note > 0)
                .average()
                .orElse(0.0);
            int mitNoten = (int) studiengangStudenten.stream()
                .filter(s -> s.berechneNotendurchschnitt() > 0)
                .count();
            
            tableData.add(new StudiengangStatistik(studiengang, count.intValue(), avgNote, mitNoten));
        });
        
        table.setItems(tableData);
        table.setPrefHeight(200);
        
        content.getChildren().addAll(pieChart, new Label("Details pro Studiengang:"), table);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }
    
    private Tab createKlausurstatistikTab() {
        Tab tab = new Tab("Klausurstatistiken");
        tab.setClosable(false);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label selectLabel = new Label("Klausur auswählen:");
        ComboBox<Klausur> klausurBox = new ComboBox<>();
        klausurBox.setItems(FXCollections.observableArrayList(klausurVerwaltung.getAlleSortiert()));
        klausurBox.setPrefWidth(400);
        klausurBox.setCellFactory(lv -> new ListCell<Klausur>() {
            @Override
            protected void updateItem(Klausur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + " - " + item.getTitel());
            }
        });
        klausurBox.setButtonCell(new ListCell<Klausur>() {
            @Override
            protected void updateItem(Klausur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + " - " + item.getTitel());
            }
        });
        
        VBox statistikBox = new VBox(10);
        statistikBox.setPadding(new Insets(20));
        statistikBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
        
        klausurBox.setOnAction(e -> {
            Klausur selected = klausurBox.getValue();
            if (selected != null) {
                aktualisiereKlausurStatistik(selected, statistikBox);
            }
        });
        
        content.getChildren().addAll(selectLabel, klausurBox, statistikBox);
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createLeistungsTab() {
        Tab tab = new Tab("Leistungsübersicht");
        tab.setClosable(false);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Studenten nach Leistung");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Tabs für verschiedene Kategorien
        TabPane leistungTabs = new TabPane();
        
        // Beste Studenten
        Tab besteTab = new Tab("Beste Studenten");
        besteTab.setClosable(false);
        ListView<String> besteList = new ListView<>();
        
        List<Student> besteStudenten = studentenVerwaltung.getAlleSortiert(ErweiterteStudentenVerwaltung.SortierKriterium.NACHNAME)
            .stream()
            .filter(s -> s.berechneNotendurchschnitt() > 0 && s.berechneNotendurchschnitt() <= 2.0)
            .sorted(Comparator.comparing(Student::berechneNotendurchschnitt))
            .limit(10)
            .collect(Collectors.toList());
        
        ObservableList<String> besteItems = FXCollections.observableArrayList();
        besteStudenten.forEach(s -> 
            besteItems.add(String.format("%.2f - %s (%s)", 
                s.berechneNotendurchschnitt(), 
                s.getNachname() + ", " + s.getVorname(),
                s.getStudiengang()))
        );
        
        besteList.setItems(besteItems);
        besteTab.setContent(besteList);
        
        // Gefährdete Studenten
        Tab gefaehrdeteTab = new Tab("Gefährdete Studenten");
        gefaehrdeteTab.setClosable(false);
        ListView<String> gefaehrdeteList = new ListView<>();
        
        List<Student> gefaehrdeteStudenten = studentenVerwaltung.getStudentenMitSchlechtenLeistungen();
        
        ObservableList<String> gefaehrdeteItems = FXCollections.observableArrayList();
        gefaehrdeteStudenten.forEach(s -> 
            gefaehrdeteItems.add(String.format("%.2f - %s (%s) - WARNUNG", 
                s.berechneNotendurchschnitt(), 
                s.getNachname() + ", " + s.getVorname(),
                s.getStudiengang()))
        );
        
        gefaehrdeteList.setItems(gefaehrdeteItems);
        gefaehrdeteList.setStyle("-fx-control-inner-background: #fff5f5;");
        gefaehrdeteTab.setContent(gefaehrdeteList);
        
        leistungTabs.getTabs().addAll(besteTab, gefaehrdeteTab);
        
        content.getChildren().addAll(titleLabel, leistungTabs);
        
        tab.setContent(content);
        return tab;
    }
    
    private VBox createStatBox(String title, String value, String subtitle) {
        VBox box = new VBox(5);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setPrefSize(200, 100);
        box.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        box.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        
        return box;
    }
    
    private void aktualisiereKlausurStatistik(Klausur klausur, VBox container) {
        container.getChildren().clear();
        
        VersuchsVerwaltung.KlausurStatistik stats = 
            studentenVerwaltung.getVersuchsVerwaltung().berechneStatistik(klausur);
        
        Label klausurLabel = new Label(klausur.getTitel());
        klausurLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        
        grid.add(new Label("Gesamtversuche:"), 0, 0);
        grid.add(new Label(String.valueOf(stats.gesamtVersuche)), 1, 0);
        
        grid.add(new Label("Bestanden:"), 0, 1);
        grid.add(new Label(stats.bestanden + " (" + String.format("%.1f%%", stats.bestehendenquote) + ")"), 1, 1);
        
        grid.add(new Label("Nicht bestanden:"), 0, 2);
        grid.add(new Label(String.valueOf(stats.nichtBestanden)), 1, 2);
        
        grid.add(new Label("Durchschnittsnote:"), 0, 3);
        Label avgLabel = new Label(stats.durchschnittsnote > 0 ? String.format("%.2f", stats.durchschnittsnote) : "-");
        if (stats.durchschnittsnote > 0 && stats.durchschnittsnote <= 2.0) {
            avgLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else if (stats.durchschnittsnote > 3.0) {
            avgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
        grid.add(avgLabel, 1, 3);
        
        grid.add(new Label("Angemeldete Studenten:"), 0, 4);
        grid.add(new Label(String.valueOf(klausur.getTeilnehmendeStudenten().size())), 1, 4);
        
        container.getChildren().addAll(klausurLabel, grid);
        
        if (stats.gesamtVersuche > 0) {
            // Notenverteilung für diese Klausur
            BarChart<String, Number> notenChart = createKlausurNotenChart(klausur);
            notenChart.setPrefHeight(300);
            container.getChildren().add(notenChart);
        }
    }
    
    private BarChart<String, Number> createKlausurNotenChart(Klausur klausur) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Note");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Anzahl");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Notenverteilung");
        chart.setLegendVisible(false);
        
        Map<String, Integer> notenCount = new TreeMap<>();
        
        studentenVerwaltung.getVersuchsVerwaltung().getVersucheFuerKlausur(klausur)
            .forEach(v -> {
                String noteStr = String.format("%.1f", v.getNote());
                notenCount.merge(noteStr, 1, Integer::sum);
            });
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        notenCount.forEach((note, count) -> 
            series.getData().add(new XYChart.Data<>(note, count))
        );
        
        chart.getData().add(series);
        
        return chart;
    }
    
    // Hilfsklasse für Studiengangstatistik
    private static class StudiengangStatistik {
        final String studiengang;
        final int anzahl;
        final double durchschnitt;
        final int mitNoten;
        
        StudiengangStatistik(String studiengang, int anzahl, double durchschnitt, int mitNoten) {
            this.studiengang = studiengang;
            this.anzahl = anzahl;
            this.durchschnitt = durchschnitt;
            this.mitNoten = mitNoten;
        }
    }
}