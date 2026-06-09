package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.EventApiClient;
import com.npopov.philharmonic.client.model.EventModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import com.npopov.philharmonic.client.ui.dialog.EventDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.function.Predicate;

public class EventsTabController extends BaseTabController<EventModel> {

    @Override
    protected void buildColumns() {
        TableColumn<EventModel, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<EventModel, String> titleCol = new TableColumn<>("Название");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<EventModel, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        typeCol.setPrefWidth(120);

        TableColumn<EventModel, String> venueCol = new TableColumn<>("Площадка");
        venueCol.setCellValueFactory(new PropertyValueFactory<>("venueName"));
        venueCol.setPrefWidth(150);

        TableColumn<EventModel, String> startCol = new TableColumn<>("Начало");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDatetime"));
        startCol.setPrefWidth(150);

        TableColumn<EventModel, String> endCol = new TableColumn<>("Конец");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDatetime"));
        endCol.setPrefWidth(150);

        tableView.getColumns().addAll(idCol, titleCol, typeCol, venueCol, startCol, endCol);
    }

    @Override
    protected List<EventModel> loadData() {
        return EventApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<EventModel> buildFilter(String searchText) {
        return e -> {
            if (searchText == null || searchText.isBlank()) return true;
            String lower = searchText.toLowerCase();
            return (e.getTitle() != null && e.getTitle().toLowerCase().contains(lower)) ||
                    (e.getEventType() != null && e.getEventType().toLowerCase().contains(lower)) ||
                    (e.getVenueName() != null && e.getVenueName().toLowerCase().contains(lower));
        };
    }

    @Override
    protected void showCreateDialog() {
        EventDialog dialog = new EventDialog(null);
        dialog.showAndWait().ifPresent(body -> {
            try {
                EventApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    @Override
    protected void showEditDialog(EventModel item) {
        EventDialog dialog = new EventDialog(item);
        dialog.showAndWait().ifPresent(body -> {
            try {
                EventApiClient.getInstance().update(item.getId(), body);
                refresh();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    @Override
    protected void deleteSelected(EventModel item) {
        EventApiClient.getInstance().delete(item.getId());
    }
}