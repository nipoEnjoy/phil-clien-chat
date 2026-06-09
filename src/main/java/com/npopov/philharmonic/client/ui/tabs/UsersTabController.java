package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.RoleApiClient;
import com.npopov.philharmonic.client.api.UserAdminApiClient;
import com.npopov.philharmonic.client.model.RoleModel;
import com.npopov.philharmonic.client.model.UserModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UsersTabController extends BaseTabController<UserModel> {

    @Override
    protected void buildColumns() {
        TableColumn<UserModel, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<UserModel, String> colUsername = new TableColumn<>("Логин");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsername.setPrefWidth(150);

        TableColumn<UserModel, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<UserModel, Boolean> colEnabled = new TableColumn<>("Активен");
        colEnabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        colEnabled.setPrefWidth(80);

        TableColumn<UserModel, String> colRoles = new TableColumn<>("Роли");
        colRoles.setCellValueFactory(cd -> {
            String roles = cd.getValue().getRoles().stream()
                    .map(RoleModel::getName)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(roles);
        });
        colRoles.setPrefWidth(200);

        tableView.getColumns().addAll(colId, colUsername, colEmail, colEnabled, colRoles);
    }

    @Override
    protected List<UserModel> loadData() {
        return UserAdminApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<UserModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return u -> true;
        return u -> str(u.getUsername()).toLowerCase().contains(q) ||
                str(u.getEmail()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        userDialog(null).showAndWait().ifPresent(body -> {
            try {
                UserAdminApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(UserModel item) {
        userDialog(item).showAndWait().ifPresent(body -> {
            try {
                UserAdminApiClient.getInstance().update(item.getId(), body);
                // Если в теле есть roleIds, обновим отдельно
                if (body.containsKey("roleIds")) {
                    @SuppressWarnings("unchecked")
                    Set<Long> roleIds = (Set<Long>) body.get("roleIds");
                    UserAdminApiClient.getInstance().assignRoles(item.getId(), roleIds);
                }
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(UserModel item) {
        UserAdminApiClient.getInstance().delete(item.getId());
    }

    private Dialog<Map<String, Object>> userDialog(UserModel existing) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новый пользователь" : "Редактировать пользователя");
        dialog.setHeaderText(null);

        TextField usernameField = new TextField(existing != null ? existing.getUsername() : "");
        TextField emailField    = new TextField(existing != null ? existing.getEmail() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль (только при создании)");
        CheckBox enabledCheck = new CheckBox("Активен");
        if (existing != null) enabledCheck.setSelected(existing.getEnabled());

        // ComboBox для множественного выбора ролей
        ListView<RoleModel> rolesList = new ListView<>();
        rolesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Загружаем все роли
        try {
            List<RoleModel> allRoles = RoleApiClient.getInstance().findAll();
            rolesList.setItems(FXCollections.observableArrayList(allRoles));
            if (existing != null) {
                Set<Long> existingRoleIds = existing.getRoles().stream()
                        .map(RoleModel::getId)
                        .collect(Collectors.toSet());
                rolesList.getSelectionModel().clearSelection();
                for (int i = 0; i < allRoles.size(); i++) {
                    if (existingRoleIds.contains(allRoles.get(i).getId())) {
                        rolesList.getSelectionModel().select(i);
                    }
                }
            }
        } catch (Exception e) {
            rolesList.setPlaceholder(new Label("Ошибка загрузки ролей"));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Логин *"), usernameField);
        grid.addRow(1, new Label("Email"), emailField);
        if (existing == null) {
            grid.addRow(2, new Label("Пароль *"), passwordField);
        }
        grid.addRow(3, new Label("Активен"), enabledCheck);
        grid.addRow(4, new Label("Роли"), rolesList);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);



        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(usernameField.getText().isBlank() ||
                (existing == null && passwordField.getText().isBlank()));
        usernameField.textProperty().addListener((o, ov, nv) -> okBtn.setDisable(nv.isBlank()));
        if (existing == null) {
            passwordField.textProperty().addListener((o, ov, nv) ->
                    okBtn.setDisable(usernameField.getText().isBlank() || nv.isBlank()));
        }

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> data = new HashMap<>();
            data.put("username", usernameField.getText().trim());
            data.put("email", emailField.getText().trim());
            data.put("enabled", enabledCheck.isSelected());
            if (existing == null) {
                data.put("password", passwordField.getText());
            }
            Set<Long> selectedRoleIds = rolesList.getSelectionModel().getSelectedItems().stream()
                    .map(RoleModel::getId)
                    .collect(Collectors.toSet());
            data.put("roleIds", selectedRoleIds);
            return data;
        });

        return dialog;
    }
}