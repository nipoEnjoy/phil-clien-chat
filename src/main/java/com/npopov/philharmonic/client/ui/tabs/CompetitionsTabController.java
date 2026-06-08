package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.CompetitionApiClient;
import com.npopov.philharmonic.client.model.CompetitionModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CompetitionsTabController extends BaseTabController<CompetitionModel> {

    @Override
    protected void buildColumns() {
        tableView.getColumns().addAll(
                col("ID",          60,  "id"),
                col("ID события",  100, "eventId"),
                col("Тип",         160, "competitionType"),
                col("Правила",     200, "rules"),
                col("Жюри",        200, "juryInfo")
        );
    }

    @Override
    protected List<CompetitionModel> loadData() {
        return CompetitionApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<CompetitionModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return c -> str(c.getCompetitionType()).toLowerCase().contains(q)
                || str(c.getRules()).toLowerCase().contains(q)
                || str(c.getJuryInfo()).toLowerCase().contains(q)
                || str(c.getEventId()).contains(q);
    }

    @Override
    protected void showCreateDialog() {
        dialog(null).showAndWait().ifPresent(body -> {
            try { CompetitionApiClient.getInstance().create(body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(CompetitionModel item) {
        dialog(item).showAndWait().ifPresent(body -> {
            try { CompetitionApiClient.getInstance().update(item.getId(), body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(CompetitionModel item) {
        CompetitionApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<CompetitionModel, V> col(String t, double w, String p) {
        TableColumn<CompetitionModel, V> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }

    private Dialog<Map<String, Object>> dialog(CompetitionModel ex) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(ex == null ? "Новый конкурс" : "Редактировать конкурс");
        d.setHeaderText(null);
        TextField eventId = field(ex != null ? str(ex.getEventId())          : "");
        TextField type    = field(ex != null ? str(ex.getCompetitionType())  : "");
        TextField rules   = field(ex != null ? str(ex.getRules())            : "");
        TextField jury    = field(ex != null ? str(ex.getJuryInfo())         : "");
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(10); g.setVgap(8);
        g.addRow(0, lbl("ID события *"), eventId);
        g.addRow(1, lbl("Тип"),          type);
        g.addRow(2, lbl("Правила"),      rules);
        g.addRow(3, lbl("Жюри"),         jury);
        g.setPadding(new javafx.geometry.Insets(16)); eventId.setPrefWidth(220);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            java.util.HashMap<String, Object> m = new java.util.HashMap<>();
            m.put("eventId",         parseLong(eventId.getText()));
            m.put("competitionType", type.getText().trim());
            m.put("rules",           rules.getText().trim());
            m.put("juryInfo",        jury.getText().trim());
            return m;
        });
        return d;
    }

    private static Long parseLong(String s) { try { return Long.parseLong(s.trim()); } catch (Exception e) { return null; } }
    private TextField field(String v) { TextField tf = new TextField(v); tf.getStyleClass().add("text-field-lg"); return tf; }
    private Label lbl(String t)       { Label l = new Label(t); l.getStyleClass().add("field-label"); return l; }
    protected static String str(Object o) { return o == null ? "" : o.toString(); }
}
