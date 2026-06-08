package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.ArtistApiClient;
import com.npopov.philharmonic.client.model.ArtistModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class ArtistsTabController extends BaseTabController<ArtistModel> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    protected void buildColumns() {
        TableColumn<ArtistModel, Long>   colId        = col("ID",           60,  "id");
        TableColumn<ArtistModel, String> colFirst     = col("Имя",          120, "firstName");
        TableColumn<ArtistModel, String> colLast      = col("Фамилия",      120, "lastName");
        TableColumn<ArtistModel, String> colStage     = col("Псевдоним",    140, "stageName");
        TableColumn<ArtistModel, String> colContact   = col("Контакт",      160, "contactInfo");

        TableColumn<ArtistModel, String> colCreated = new TableColumn<>("Создан");
        colCreated.setPrefWidth(130);
        colCreated.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        cd.getValue().getCreatedAt() != null
                                ? cd.getValue().getCreatedAt().format(FMT) : ""));

        tableView.getColumns().addAll(colId, colFirst, colLast, colStage, colContact, colCreated);
        tableView.getSortOrder().add(colId);
    }

    @Override
    protected List<ArtistModel> loadData() {
        return ArtistApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<ArtistModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return a -> str(a.getFirstName()).toLowerCase().contains(q)
                || str(a.getLastName()).toLowerCase().contains(q)
                || str(a.getStageName()).toLowerCase().contains(q)
                || str(a.getContactInfo()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        ArtistDialog dialog = new ArtistDialog(null);
        dialog.showAndWait().ifPresent(body -> {
            try {
                ArtistApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(ArtistModel item) {
        ArtistDialog dialog = new ArtistDialog(item);
        dialog.showAndWait().ifPresent(body -> {
            try {
                System.out.println(str(body));
                ArtistApiClient.getInstance().update(item.getId(), body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(ArtistModel item) {
        ArtistApiClient.getInstance().delete(item.getId());
    }

    // ── Column helper ────────────────────────────────────────────────────────
    private <V> TableColumn<ArtistModel, V> col(String title, double width, String prop) {
        TableColumn<ArtistModel, V> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    // ── Inner dialog ─────────────────────────────────────────────────────────
    private static class ArtistDialog extends Dialog<Map<String, Object>> {
        ArtistDialog(ArtistModel existing) {
            setTitle(existing == null ? "Новый артист" : "Редактировать артиста");
            setHeaderText(null);

            TextField firstName   = field(existing != null ? existing.getFirstName()   : "");
            TextField lastName    = field(existing != null ? existing.getLastName()    : "");
            TextField stageName   = field(existing != null ? existing.getStageName()   : "");
            TextField contactInfo = field(existing != null ? existing.getContactInfo() : "");

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(8);
            grid.addRow(0, label("Имя *"),      firstName);
            grid.addRow(1, label("Фамилия"),    lastName);
            grid.addRow(2, label("Псевдоним"),  stageName);
            grid.addRow(3, label("Контакт"),    contactInfo);
            grid.setPadding(new javafx.geometry.Insets(16));
            firstName.setPrefWidth(220);

            getDialogPane().setContent(grid);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());

            Button okBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okBtn.setDisable(true);
            firstName.textProperty().addListener((o, ov, nv) ->
                    okBtn.setDisable(nv == null || nv.isBlank()));
            if (existing != null && existing.getFirstName() != null && !existing.getFirstName().isBlank())
                okBtn.setDisable(false);

            setResultConverter(btn -> {
                if (btn != ButtonType.OK) return null;
                return Map.of(
                        "firstName",   firstName.getText().trim(),
                        "lastName",    lastName.getText().trim(),
                        "stageName",   stageName.getText().trim(),
                        "contactInfo", contactInfo.getText().trim()
                );
            });
        }

        private TextField field(String val) {
            TextField tf = new TextField(val);
            tf.getStyleClass().add("text-field-lg");
            return tf;
        }
        private Label label(String text) {
            Label l = new Label(text);
            l.getStyleClass().add("field-label");
            return l;
        }
    }
}
