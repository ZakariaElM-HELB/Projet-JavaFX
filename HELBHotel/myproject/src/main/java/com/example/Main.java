package com.example;


import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            new HELBHotelController(primaryStage); // Si erreur ici → catch
        } catch (IllegalArgumentException | IOException e) {
            // Affiche l’erreur (console ou Alert si tu veux) — optionnel
            System.err.println("Erreur critique au démarrage : " + e.getMessage());
            // Ferme immédiatement l'application
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
