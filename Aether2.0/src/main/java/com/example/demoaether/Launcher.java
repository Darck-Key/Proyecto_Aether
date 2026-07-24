package com.example.demoaether;

import javafx.application.Application;

/**
 * Entrada compatible con Gradle e IntelliJ.
 *
 * Quien llama:
 * - La tarea run de Gradle o la configuracion de ejecucion de IntelliJ.
 *
 * A quien llama:
 * - HelloApplication, que carga el FXML y muestra JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        // Mantiene main() separado de Application para evitar problemas de lanzamiento modular.
        Application.launch(HelloApplication.class, args);
    }
}
