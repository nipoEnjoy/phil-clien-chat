package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.RoleApiClient;
import com.npopov.philharmonic.client.model.CrudPermissions;
import com.npopov.philharmonic.client.model.RoleModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RolesTabController extends BaseTabController<RoleModel> {

    @Override
    protected void buildColumns() {
        TableColumn<RoleModel, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<RoleModel, String> colName = new TableColumn<>("Название");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(180);

        TableColumn<RoleModel, String> colDesc = new TableColumn<>("Описание");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(250);

        TableColumn<RoleModel, String> colPerms = new TableColumn<>("Права");
        colPerms.setCellValueFactory(cd -> {
            String perms = cd.getValue().getPermissions().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(perms);
        });
        colPerms.setPrefWidth(200);

        tableView.getColumns().addAll(colId, colName, colDesc, colPerms);
    }

    @Override
    protected List<RoleModel> loadData() {
        return RoleApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<RoleModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return r -> true;
        return r -> str(r.getName()).toLowerCase().contains(q) ||
                str(r.getDescription()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        roleDialog(null).showAndWait().ifPresent(body -> {
            try {
                RoleApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(RoleModel item) {
        roleDialog(item).showAndWait().ifPresent(body -> {
            try {
                RoleApiClient.getInstance().update(item.getId(), body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(RoleModel item) {
        RoleApiClient.getInstance().delete(item.getId());
    }

    private Dialog<Map<String, Object>> roleDialog(RoleModel existing) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новая роль" : "Редактировать роль");
        dialog.setHeaderText(null);

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Название роли");
        TextArea descArea = new TextArea(existing != null ? existing.getDescription() : "");
        descArea.setPrefRowCount(2);

        CheckBox chkCreate = new CheckBox("CREATE");
        CheckBox chkRead   = new CheckBox("READ");
        CheckBox chkUpdate = new CheckBox("UPDATE");
        CheckBox chkDelete = new CheckBox("DELETE");

        if (existing != null) {
            Set<CrudPermissions> perms = existing.getPermissions();
            chkCreate.setSelected(perms.contains(CrudPermissions.CREATE));
            chkRead.setSelected(perms.contains(CrudPermissions.READ));
            chkUpdate.setSelected(perms.contains(CrudPermissions.UPDATE));
            chkDelete.setSelected(perms.contains(CrudPermissions.DELETE));
        } else {
            // По умолчанию для новой роли – только READ
            chkRead.setSelected(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Название *"), nameField);
        grid.addRow(1, new Label("Описание"), descArea);
        grid.addRow(2, new Label("Права"), new HBox(10, chkCreate, chkRead, chkUpdate, chkDelete));

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(nameField.getText().isBlank());
        nameField.textProperty().addListener((o, ov, nv) -> okBtn.setDisable(nv.isBlank()));

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> data = new HashMap<>();
            data.put("name", nameField.getText().trim());
            data.put("description", descArea.getText().trim());

            Set<CrudPermissions> selectedPerms = new HashSet<>();
            if (chkCreate.isSelected()) selectedPerms.add(CrudPermissions.CREATE);
            if (chkRead.isSelected())   selectedPerms.add(CrudPermissions.READ);
            if (chkUpdate.isSelected()) selectedPerms.add(CrudPermissions.UPDATE);
            if (chkDelete.isSelected()) selectedPerms.add(CrudPermissions.DELETE);
            data.put("permissions", selectedPerms);
            return data;
        });

        return dialog;
    }
}