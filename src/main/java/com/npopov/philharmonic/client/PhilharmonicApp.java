package com.npopov.philharmonic.client;

import com.npopov.philharmonic.client.util.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 * Opens the login screen; the main window loads after successful auth.
 */
public class PhilharmonicApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, AppConfig.LOGIN_WIDTH, AppConfig.LOGIN_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());

        primaryStage.setTitle("Филармония — Вход");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
