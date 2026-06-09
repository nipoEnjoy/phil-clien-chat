package com.npopov.philharmonic.client.ui.main;

import com.npopov.philharmonic.client.session.SessionManager;
import com.npopov.philharmonic.client.util.AppConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private Label    usernameLabel;
    @FXML private Label    roleLabel;
    @FXML private Label    statusLabel;
    @FXML private Label    serverLabel;
    @FXML private TabPane  tabPane;

    @FXML private StackPane eventsPane;
    @FXML private StackPane artistsPane;
    @FXML private StackPane venuesPane;
    @FXML private StackPane impresariosPane;
    @FXML private StackPane organizersPane;
    @FXML private StackPane competitionsPane;
    @FXML private StackPane resultsPane;
    @FXML private StackPane genresPane;
    @FXML private StackPane usersPane;
    @FXML private StackPane rolesPane;
    @FXML private StackPane reportsPane;

    private final boolean[] loaded = new boolean[11];

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        usernameLabel.setText(session.getUsername());
        roleLabel.setText(formatRole(session.getRoles().toString()));
        serverLabel.setText("Сервер: " + AppConfig.BASE_URL);

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> loadTabIfNeeded(newTab));

        loadTabIfNeeded(tabPane.getSelectionModel().getSelectedItem());

        if (!SessionManager.getInstance().isSuperAdmin()) {
            tabPane.getTabs().removeIf(tab -> tab.getText().equals("Пользователи") || tab.getText().equals("Роли"));
        }
    }

    private void loadTabIfNeeded(Tab tab) {
        if (tab == null) return;
        int idx = tabPane.getTabs().indexOf(tab);
        if (idx < 0 || loaded[idx]) return;
        loaded[idx] = true;

        String fxmlPath = switch (idx) {
            case 0 -> "/fxml/tabs/events.fxml";
            case 1 -> "/fxml/tabs/artists.fxml";
            case 2 -> "/fxml/tabs/venues.fxml";
            case 3 -> "/fxml/tabs/impresarios.fxml";
            case 4 -> "/fxml/tabs/organizers.fxml";
            case 5 -> "/fxml/tabs/competitions.fxml";
            case 6 -> "/fxml/tabs/results.fxml";
            case 7 -> "/fxml/tabs/genres.fxml";
            case 8 -> "/fxml/tabs/users.fxml";
            case 9 -> "/fxml/tabs/roles.fxml";
            case 10 -> "/fxml/tabs/reports.fxml";
            default -> null;
        };

        StackPane pane = switch (idx) {
            case 0 -> eventsPane;
            case 1 -> artistsPane;
            case 2 -> venuesPane;
            case 3 -> impresariosPane;
            case 4 -> organizersPane;
            case 5 -> competitionsPane;
            case 6 -> resultsPane;
            case 7 -> genresPane;
            case 8 -> usersPane;
            case 9 -> rolesPane;
            case 10 -> reportsPane;
            default -> null;
        };

        if (fxmlPath == null || pane == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            pane.getChildren().setAll(content);
            setStatus("Загружено: " + tab.getText());
        } catch (Exception ex) {
            setStatus("Ошибка загрузки вкладки: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Scene scene = new Scene(root, AppConfig.LOGIN_WIDTH, AppConfig.LOGIN_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Филармония — Вход");
            stage.centerOnScreen();
        } catch (Exception ex) {
            setStatus("Ошибка выхода: " + ex.getMessage());
        }
    }

    public void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private String formatRole(String rawRoles) {
        if (rawRoles.contains("SUPERADMIN")) return "Суперадмин";
        if (rawRoles.contains("ADMIN"))      return "Администратор";
        return "Пользователь";
    }
}
