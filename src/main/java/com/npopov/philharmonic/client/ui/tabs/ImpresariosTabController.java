package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.ImpresarioApiClient;
import com.npopov.philharmonic.client.model.ImpresarioModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ImpresariosTabController extends BaseTabController<ImpresarioModel> {

    @Override
    protected void buildColumns() {
        tableView.getColumns().addAll(
                col("ID",           60,  "id"),
                col("Имя",         120,  "firstName"),
                col("Фамилия",     120,  "lastName"),
                col("Организация", 180,  "organization"),
                col("Контакт",     180,  "contactInfo")
        );
    }

    @Override
    protected List<ImpresarioModel> loadData() {
        return ImpresarioApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<ImpresarioModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return i -> str(i.getFirstName()).toLowerCase().contains(q)
                || str(i.getLastName()).toLowerCase().contains(q)
                || str(i.getOrganization()).toLowerCase().contains(q)
                || str(i.getContactInfo()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        dialog(null).showAndWait().ifPresent(body -> {
            try { ImpresarioApiClient.getInstance().create(body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(ImpresarioModel item) {
        dialog(item).showAndWait().ifPresent(body -> {
            try { ImpresarioApiClient.getInstance().update(item.getId(), body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(ImpresarioModel item) {
        ImpresarioApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<ImpresarioModel, V> col(String t, double w, String p) {
        TableColumn<ImpresarioModel, V> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }

    private Dialog<Map<String, Object>> dialog(ImpresarioModel ex) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(ex == null ? "Новый импресарио" : "Редактировать импресарио");
        d.setHeaderText(null);
        TextField fn  = field(ex != null ? str(ex.getFirstName())   : "");
        TextField ln  = field(ex != null ? str(ex.getLastName())    : "");
        TextField org = field(ex != null ? str(ex.getOrganization()): "");
        TextField ci  = field(ex != null ? str(ex.getContactInfo()) : "");
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(10); g.setVgap(8);
        g.addRow(0, lbl("Имя *"),       fn);
        g.addRow(1, lbl("Фамилия"),     ln);
        g.addRow(2, lbl("Организация"), org);
        g.addRow(3, lbl("Контакт"),     ci);
        g.setPadding(new javafx.geometry.Insets(16)); fn.setPrefWidth(220);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        ok.setDisable(fn.getText().isBlank());
        fn.textProperty().addListener((o, ov, nv) -> ok.setDisable(nv == null || nv.isBlank()));
        d.setResultConverter(btn -> btn != ButtonType.OK ? null :
                Map.of("firstName", fn.getText().trim(), "lastName", ln.getText().trim(),
                        "organization", org.getText().trim(), "contactInfo", ci.getText().trim()));
        return d;
    }

    private TextField field(String v) { TextField tf = new TextField(v); tf.getStyleClass().add("text-field-lg"); return tf; }
    private Label lbl(String t)       { Label l = new Label(t); l.getStyleClass().add("field-label"); return l; }
    protected static String str(Object o) { return o == null ? "" : o.toString(); }
}
