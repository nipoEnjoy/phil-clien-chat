package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.CompetitionResultApiClient;
import com.npopov.philharmonic.client.model.CompetitionResultModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CompetitionResultsTabController extends BaseTabController<CompetitionResultModel> {

    @Override
    protected void buildColumns() {
        tableView.getColumns().addAll(
                col("ID",           60,  "id"),
                col("Конкурс ID",  100,  "competitionId"),
                col("Артист ID",   100,  "artistId"),
                col("Артист",      180,  "artistName"),
                col("Место",        80,  "place"),
                col("Награда",     200,  "award")
        );
    }

    @Override
    protected List<CompetitionResultModel> loadData() {
        return CompetitionResultApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<CompetitionResultModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return r -> str(r.getArtistName()).toLowerCase().contains(q)
                || str(r.getAward()).toLowerCase().contains(q)
                || str(r.getCompetitionId()).contains(q);
    }

    @Override
    protected void showCreateDialog() {
        dialog(null).showAndWait().ifPresent(body -> {
            try { CompetitionResultApiClient.getInstance().create(body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(CompetitionResultModel item) {
        dialog(item).showAndWait().ifPresent(body -> {
            try { CompetitionResultApiClient.getInstance().update(item.getId(), body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(CompetitionResultModel item) {
        CompetitionResultApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<CompetitionResultModel, V> col(String t, double w, String p) {
        TableColumn<CompetitionResultModel, V> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }

    private Dialog<Map<String, Object>> dialog(CompetitionResultModel ex) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(ex == null ? "Новый результат" : "Редактировать результат");
        d.setHeaderText(null);
        TextField compId   = field(ex != null ? str(ex.getCompetitionId()) : "");
        TextField artistId = field(ex != null ? str(ex.getArtistId())      : "");
        TextField place    = field(ex != null ? str(ex.getPlace())         : "");
        TextField award    = field(ex != null ? str(ex.getAward())         : "");
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(10); g.setVgap(8);
        g.addRow(0, lbl("ID конкурса *"), compId);
        g.addRow(1, lbl("ID артиста *"),  artistId);
        g.addRow(2, lbl("Место"),         place);
        g.addRow(3, lbl("Награда"),       award);
        g.setPadding(new javafx.geometry.Insets(16)); compId.setPrefWidth(220);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            java.util.HashMap<String, Object> m = new java.util.HashMap<>();
            m.put("competitionId", parseLong(compId.getText()));
            m.put("artistId",      parseLong(artistId.getText()));
            m.put("place",         parseInt(place.getText()));
            m.put("award",         award.getText().trim());
            return m;
        });
        return d;
    }

    private static Long parseLong(String s) { try { return Long.parseLong(s.trim()); } catch (Exception e) { return null; } }
    private static Integer parseInt(String s) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; } }
    private TextField field(String v) { TextField tf = new TextField(v); tf.getStyleClass().add("text-field-lg"); return tf; }
    private Label lbl(String t)       { Label l = new Label(t); l.getStyleClass().add("field-label"); return l; }
    protected static String str(Object o) { return o == null ? "" : o.toString(); }
}
