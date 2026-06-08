package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.OrganizerApiClient;
import com.npopov.philharmonic.client.model.OrganizerModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class OrganizersTabController extends BaseTabController<OrganizerModel> {

    @Override
    protected void buildColumns() {
        tableView.getColumns().addAll(
                col("ID",       60,  "id"),
                col("Название", 200, "name"),
                col("Тип",      120, "type"),
                col("Контакт",  200, "contactInfo")
        );
    }

    @Override
    protected List<OrganizerModel> loadData() {
        return OrganizerApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<OrganizerModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return o -> str(o.getName()).toLowerCase().contains(q)
                || str(o.getType()).toLowerCase().contains(q)
                || str(o.getContactInfo()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        dialog(null).showAndWait().ifPresent(body -> {
            try { OrganizerApiClient.getInstance().create(body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(OrganizerModel item) {
        dialog(item).showAndWait().ifPresent(body -> {
            try { OrganizerApiClient.getInstance().update(item.getId(), body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(OrganizerModel item) {
        OrganizerApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<OrganizerModel, V> col(String t, double w, String p) {
        TableColumn<OrganizerModel, V> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }

    private Dialog<Map<String, Object>> dialog(OrganizerModel ex) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(ex == null ? "Новый организатор" : "Редактировать организатора");
        d.setHeaderText(null);
        TextField name = field(ex != null ? str(ex.getName())        : "");
        TextField type = field(ex != null ? str(ex.getType())        : "");
        TextField ci   = field(ex != null ? str(ex.getContactInfo()) : "");
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(10); g.setVgap(8);
        g.addRow(0, lbl("Название *"), name);
        g.addRow(1, lbl("Тип"),        type);
        g.addRow(2, lbl("Контакт"),    ci);
        g.setPadding(new javafx.geometry.Insets(16)); name.setPrefWidth(220);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        ok.setDisable(name.getText().isBlank());
        name.textProperty().addListener((o, ov, nv) -> ok.setDisable(nv == null || nv.isBlank()));
        d.setResultConverter(btn -> btn != ButtonType.OK ? null :
                Map.of("name", name.getText().trim(), "type", type.getText().trim(),
                        "contactInfo", ci.getText().trim()));
        return d;
    }

    private TextField field(String v) { TextField tf = new TextField(v); tf.getStyleClass().add("text-field-lg"); return tf; }
    private Label lbl(String t)       { Label l = new Label(t); l.getStyleClass().add("field-label"); return l; }
    protected static String str(Object o) { return o == null ? "" : o.toString(); }
}
