package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.VenueApiClient;
import com.npopov.philharmonic.client.model.VenueModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class VenuesTabController extends BaseTabController<VenueModel> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private ComboBox<String> typeFilter;

    @Override
    protected void buildColumns() {
        tableView.getColumns().addAll(
                col("ID",          60,  "id"),
                col("Название",   200,  "name"),
                col("Тип",        120,  "venueType"),
                col("Город",      120,  "city"),
                col("Адрес",      200,  "address"),
                col("Описание",   200,  "description")
        );
    }

    @Override
    @FXML
    public void initialize() {
        // Add type filter ComboBox to the filterBar before base init
        typeFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Все", "THEATRE", "CINEMA", "CONCERT_VENUE", "VARIETY_STAGE", "CULTURAL_CENTRE"));
        typeFilter.getStyleClass().add("combo-box");
        typeFilter.setPrefWidth(160);
        typeFilter.setValue("Все");
        typeFilter.setOnAction(e -> {
            if (filteredData != null && searchField != null) {
                applyTypeFilter();
            }
        });
        super.initialize();
        filterBar.getChildren().add(0, typeFilter);
    }

    private void applyTypeFilter() {
        String type  = typeFilter.getValue();
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredData.setPredicate(v ->
                ("Все".equals(type) || type.equals(v.getVenueType())) &&
                buildFilter(query).test(v));
    }

    @Override
    protected List<VenueModel> loadData() {
        return VenueApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<VenueModel> buildFilter(String q) {
        String type = typeFilter != null ? typeFilter.getValue() : "Все";
        return v -> {
            boolean typeOk = "Все".equals(type) || type.equals(v.getVenueType());
            if (q == null || q.isBlank()) return typeOk;
            return typeOk && (
                    str(v.getName()).toLowerCase().contains(q) ||
                    str(v.getCity()).toLowerCase().contains(q) ||
                    str(v.getAddress()).toLowerCase().contains(q));
        };
    }

    @Override
    protected void showCreateDialog() {
        VenueDialog d = new VenueDialog(null);
        d.showAndWait().ifPresent(body -> {
            try { VenueApiClient.getInstance().create(body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(VenueModel item) {
        VenueDialog d = new VenueDialog(item);
        d.showAndWait().ifPresent(body -> {
            try { VenueApiClient.getInstance().update(item.getId(), body); refresh(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(VenueModel item) {
        VenueApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<VenueModel, V> col(String title, double width, String prop) {
        TableColumn<VenueModel, V> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    private static class VenueDialog extends Dialog<Map<String, Object>> {
        VenueDialog(VenueModel ex) {
            setTitle(ex == null ? "Новая площадка" : "Редактировать площадку");
            setHeaderText(null);

            TextField name        = field(ex != null ? str(ex.getName())        : "");
            ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList(
                    "THEATRE","CINEMA","CONCERT_VENUE","VARIETY_STAGE","CULTURAL_CENTRE"));
            type.setValue(ex != null && ex.getVenueType() != null ? ex.getVenueType() : "THEATRE");
            type.getStyleClass().add("combo-box");
            TextField city        = field(ex != null ? str(ex.getCity())        : "");
            TextField address     = field(ex != null ? str(ex.getAddress())     : "");
            TextField description = field(ex != null ? str(ex.getDescription()) : "");

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(8);
            grid.addRow(0, label("Название *"), name);
            grid.addRow(1, label("Тип *"),      type);
            grid.addRow(2, label("Город"),      city);
            grid.addRow(3, label("Адрес"),      address);
            grid.addRow(4, label("Описание"),   description);
            grid.setPadding(new javafx.geometry.Insets(16));
            name.setPrefWidth(240);

            getDialogPane().setContent(grid);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());

            setResultConverter(btn -> {
                if (btn != ButtonType.OK) return null;
                return Map.of(
                        "name",        name.getText().trim(),
                        "venueType",   type.getValue(),
                        "city",        city.getText().trim(),
                        "address",     address.getText().trim(),
                        "description", description.getText().trim()
                );
            });
        }
        private static String str(Object o) { return o == null ? "" : o.toString(); }
        private TextField field(String val) {
            TextField tf = new TextField(val); tf.getStyleClass().add("text-field-lg"); return tf;
        }
        private Label label(String text) {
            Label l = new Label(text); l.getStyleClass().add("field-label"); return l;
        }
    }
}
