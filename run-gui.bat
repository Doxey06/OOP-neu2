@echo off
java --module-path javafx/lib --add-modules javafx.controls,javafx.fxml -cp ".;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-simple.jar" gui.KlausurverwaltungGUI
pause