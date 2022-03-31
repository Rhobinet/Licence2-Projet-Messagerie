package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Cette classe permet le lancement de la fenêtre du logiciel.
 *
 * @author Kéran Carvalhais
 * @version 1
 */

public class Messagerie extends Application {

    /**
     * Cette méthode est celle qui va définir la taille de la fenêtre, et elle va créer chaque élément par rapport à la page fxml messagerie.fxml.
     * C'est dans cette méthode qu'est définit le titre de la fenêtre, sa taille.
     *
     * @param stage la fenêtre qui apparaît
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Messagerie.class.getResource("messagerie.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setResizable(false);
        stage.setTitle("Messagerie");
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Cette méthode lance le logiciel.
     *
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }
}

