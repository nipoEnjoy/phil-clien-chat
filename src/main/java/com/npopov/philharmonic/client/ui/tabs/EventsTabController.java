package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.EventApiClient;
import com.npopov.philharmonic.client.model.EventModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import com.npopov.philharmonic.client.ui.dialog.EventDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EventsTabController extends BaseTabController<EventModel> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    protected void buildColumns() {
        TableColumn<EventModel, Long>   colId    = col("ID",       60,  "id");
        TableColumn<EventModel, String> colTitle = col("Название", 200, "title");
        TableColumn<EventModel, String> colType  = col("Тип",      110, "eventType");
        TableColumn<EventModel, String> colVenue = col("Площадка", 160, "venueName");
        TableColumn<EventModel, String> colOrg   = col("Организатор", 160, "organizerName");

        TableColumn<EventModel, String> colStart = new TableColumn<>("Начало");
        colStart.setPrefWidth(130);
        colStart.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStartDatetime() != null
                        ? cd.getValue().getStartDatetime().format(FMT) : ""));

        TableColumn<EventModel, String> colEnd = new TableColumn<>("Конец");
        colEnd.setPrefWidth(130);
        colEnd.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndDatetime() != null
                        ? cd.getValue().getEndDatetime().format(FMT) : ""));

        tableView.getColumns().addAll(colId, colTitle, colType, colVenue, colOrg, colStart, colEnd);
        tableView.getSortOrder().add(colStart);
    }

    @Override
    protected List<EventModel> loadData() {
        return EventApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<EventModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return e -> str(e.getTitle()).toLowerCase().contains(q)
                || str(e.getEventType()).toLowerCase().contains(q)
                || str(e.getVenueName()).toLowerCase().contains(q)
                || str(e.getOrganizerName()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        EventDialog dialog = new EventDialog(null);
        dialog.showAndWait().ifPresent(body -> {
            try {
                EventApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(EventModel item) {
        EventDialog dialog = new EventDialog(item);
        dialog.showAndWait().ifPresent(body -> {
            try {
                EventApiClient.getInstance().update(item.getId(), body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(EventModel item) {
        EventApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<EventModel, V> col(String title, double width, String prop) {
        TableColumn<EventModel, V> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }
}
