package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.GenreApiClient;
import com.npopov.philharmonic.client.model.GenreModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class GenresTabController extends BaseTabController<GenreModel> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    protected void buildColumns() {
        TableColumn<GenreModel, Long>   colId   = col("ID",          60,  "id");
        TableColumn<GenreModel, String> colName = col("Название",    220, "name");
        TableColumn<GenreModel, String> colDesc = col("Описание",    340, "description");

        TableColumn<GenreModel, String> colCreated = new TableColumn<>("Создан");
        colCreated.setPrefWidth(130);
        colCreated.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreatedAt() != null
                        ? cd.getValue().getCreatedAt().format(FMT) : ""));

        tableView.getColumns().addAll(colId, colName, colDesc, colCreated);
        tableView.getSortOrder().add(colName);
    }

    @Override
    protected List<GenreModel> loadData() {
        return GenreApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<GenreModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return g -> str(g.getName()).toLowerCase().contains(q)
                || str(g.getDescription()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        genreDialog(null).showAndWait().ifPresent(body -> {
            try { GenreApiClient.getInstance().create(body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(GenreModel item) {
        genreDialog(item).showAndWait().ifPresent(body -> {
            try { GenreApiClient.getInstance().update(item.getId(), body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(GenreModel item) {
        GenreApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<GenreModel, V> col(String title, double width, String prop) {
        TableColumn<GenreModel, V> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    private Dialog<Map<String, Object>> genreDialog(GenreModel ex) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(ex == null ? "Новый жанр" : "Редактировать жанр");
        d.setHeaderText(null);

        TextField name = field(ex != null ? str(ex.getName()) : "");
        TextArea  desc = new TextArea(ex != null ? str(ex.getDescription()) : "");
        desc.setPrefRowCount(3);
        desc.setWrapText(true);
        desc.getStyleClass().add("text-field-lg");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, lbl("Название *"), name);
        grid.addRow(1, lbl("Описание"),   desc);
        name.setPrefWidth(280);
        desc.setPrefWidth(280);

        d.getDialogPane().setContent(grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());

        Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        ok.setDisable(name.getText().isBlank());
        name.textProperty().addListener((o, ov, nv) ->
                ok.setDisable(nv == null || nv.isBlank()));

        d.setResultConverter(btn -> btn != ButtonType.OK ? null : Map.of(
                "name",        name.getText().trim(),
                "description", desc.getText().trim()
        ));
        return d;
    }

    private TextField field(String v) {
        TextField tf = new TextField(v);
        tf.getStyleClass().add("text-field-lg");
        return tf;
    }

    private Label lbl(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("field-label");
        return l;
    }
}