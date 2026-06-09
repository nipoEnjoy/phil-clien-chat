package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.CompetitionApiClient;
import com.npopov.philharmonic.client.api.CompetitionResultApiClient;
import com.npopov.philharmonic.client.model.CompetitionModel;
import com.npopov.philharmonic.client.model.CompetitionResultModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import com.npopov.philharmonic.client.ui.dialog.CompetitionResultDialog;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CompetitionResultsTabController extends BaseTabController<CompetitionResultModel> {

    private ComboBox<CompetitionModel> competitionFilter;

    @Override
    @FXML
    public void initialize() {
        competitionFilter = new ComboBox<>();
        competitionFilter.setPromptText("Конкурс");

        Button applyBtn = new Button("🔍 Показать призёров");
        applyBtn.setOnAction(e -> refresh());
        Button resetBtn = new Button("✖ Сброс");
        resetBtn.setOnAction(e -> {
            competitionFilter.getSelectionModel().clearSelection();
            refresh();
        });

        filterBar.getChildren().addAll(competitionFilter, applyBtn, resetBtn);

        loadCompetitions();

        super.initialize();
    }

    private void loadCompetitions() {
        Task<List<CompetitionModel>> task = new Task<>() {
            @Override protected List<CompetitionModel> call() { return CompetitionApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            competitionFilter.setItems(FXCollections.observableArrayList(task.getValue()));
            competitionFilter.setConverter(new StringConverter<>() {
                @Override public String toString(CompetitionModel c) { return c == null ? "" : c.getTitle(); }
                @Override public CompetitionModel fromString(String s) { return null; }
            });
        });
        new Thread(task).start();
    }

    @Override
    protected List<CompetitionResultModel> loadData() {
        CompetitionModel competition = competitionFilter.getValue();
        if (competition != null) {
            return CompetitionResultApiClient.getInstance().findByCompetition(competition.getId());
        } else {
            return CompetitionResultApiClient.getInstance().findAll();
        }
    }

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
    protected Predicate<CompetitionResultModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return r -> str(r.getArtistName()).toLowerCase().contains(q)
                || str(r.getAward()).toLowerCase().contains(q)
                || str(r.getCompetitionId()).contains(q);
    }

    @Override
    protected void showCreateDialog() {
        CompetitionResultDialog dialog = new CompetitionResultDialog(null);
        dialog.showAndWait().ifPresent(body -> {
            try {
                CompetitionResultApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(CompetitionResultModel item) {
        CompetitionResultDialog dialog = new CompetitionResultDialog(item);
        dialog.showAndWait().ifPresent(body -> {
            try {
                CompetitionResultApiClient.getInstance().update(item.getId(), body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
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
