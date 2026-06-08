package com.npopov.philharmonic.client.ui.auth;

import com.npopov.philharmonic.client.api.ApiException;
import com.npopov.philharmonic.client.api.AuthApiClient;
import com.npopov.philharmonic.client.session.SessionManager;
import com.npopov.philharmonic.client.util.AppConfig;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for login.fxml.
 * Runs the HTTP login request on a background thread to keep the UI responsive.
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    @FXML private VBox          formBox;
    @FXML private ProgressIndicator spinner;
    @FXML private Label         footerLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        spinner.setVisible(false);

        // Server info
        footerLabel.setText("Server: " + AppConfig.BASE_URL);

        // Allow login with Enter key from password field
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Fade-in on open
        formBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), formBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();


    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Введите логин и пароль");
            return;
        }

        setLoading(true);

        Task<AuthApiClient.LoginResult> task = new Task<>() {
            @Override
            protected AuthApiClient.LoginResult call() {
                return AuthApiClient.getInstance().login(username, password);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AuthApiClient.LoginResult result = task.getValue();
            SessionManager.getInstance().login(
                    result.getToken(),
                    username,
                    result.getRolesAsSet()
            );
            navigateToMain();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            Throwable ex = task.getException();
            switch (ex) {
                case ApiException apiEx when apiEx.isUnauthorized() -> {
                    showError("Неверный логин или пароль");
                }
                case ApiException apiEx when apiEx.getStatusCode() == 0 -> {
                    showError("Сервер недоступен. Проверьте подключение.");
                }
                default -> {
                    showError("Ошибка: " + ex.getMessage());
                }
            }
            shakeForm();
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, AppConfig.MAIN_WIDTH, AppConfig.MAIN_HEIGHT);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Филармония — " + SessionManager.getInstance().getUsername());
            stage.centerOnScreen();
        } catch (Exception ex) {
            showError("Не удалось открыть главное окно: " + ex.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        spinner.setVisible(loading);
        if (loading) errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void shakeForm() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), formBox);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
