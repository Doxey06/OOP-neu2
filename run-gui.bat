@echo off
rem Compile sources so the latest GUI features are available
javac --module-path javafx/lib --add-modules javafx.controls,javafx.fxml -cp ".;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-simple.jar" -d . src\util\Database.java src\verwaltung\*.java src\model\*.java src\gui\*.java src\gui\views\*.java

rem Start the application
java --module-path javafx/lib --add-modules javafx.controls,javafx.fxml -cp ".;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-simple.jar" gui.KlausurverwaltungGUI
pause
