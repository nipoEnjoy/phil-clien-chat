package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.EventApiClient;
import com.npopov.philharmonic.client.api.OrganizerApiClient;
import com.npopov.philharmonic.client.api.VenueApiClient;
import com.npopov.philharmonic.client.model.EventModel;
import com.npopov.philharmonic.client.model.OrganizerModel;
import com.npopov.philharmonic.client.model.VenueModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import com.npopov.philharmonic.client.ui.dialog.EventDialog;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public class EventsTabController extends BaseTabController<EventModel> {

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<OrganizerModel> organizerFilter;
    private ComboBox<VenueModel> venueFilter;

    @Override
    @FXML
    public void initialize() {
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("С даты");
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("По дату");
        organizerFilter = new ComboBox<>();
        organizerFilter.setPromptText("Организатор");
        venueFilter = new ComboBox<>();
        venueFilter.setPromptText("Площадка");

        Button applyBtn = new Button("🔍 Найти");
        applyBtn.setOnAction(e -> refresh());
        Button resetBtn = new Button("✖ Сброс");
        resetBtn.setOnAction(e -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            organizerFilter.getSelectionModel().clearSelection();
            venueFilter.getSelectionModel().clearSelection();
            refresh();
        });

        filterBar.getChildren().addAll(startDatePicker, endDatePicker, organizerFilter, venueFilter, applyBtn, resetBtn);

        loadOrganizers();
        loadVenues();

        super.initialize();
    }

    private void loadOrganizers() {
        Task<List<OrganizerModel>> task = new Task<>() {
            @Override protected List<OrganizerModel> call() { return OrganizerApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            organizerFilter.setItems(FXCollections.observableArrayList(task.getValue()));
            organizerFilter.setConverter(new StringConverter<>() {
                @Override public String toString(OrganizerModel o) { return o == null ? "" : o.getName(); }
                @Override public OrganizerModel fromString(String s) { return null; }
            });
        });
        new Thread(task).start();
    }

    private void loadVenues() {
        Task<List<VenueModel>> task = new Task<>() {
            @Override protected List<VenueModel> call() { return VenueApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            venueFilter.setItems(FXCollections.observableArrayList(task.getValue()));
            venueFilter.setConverter(new StringConverter<>() {
                @Override public String toString(VenueModel v) { return v == null ? "" : v.getName(); }
                @Override public VenueModel fromString(String s) { return null; }
            });
        });
        new Thread(task).start();
    }

    @Override
    protected List<EventModel> loadData() {
        LocalDateTime start = startDatePicker.getValue() != null ? startDatePicker.getValue().atStartOfDay() : null;
        LocalDateTime end = endDatePicker.getValue() != null ? endDatePicker.getValue().atTime(23, 59, 59) : null;
        OrganizerModel organizer = organizerFilter.getValue();
        VenueModel venue = venueFilter.getValue();

        if (start != null && end != null) {
            return EventApiClient.getInstance().findByPeriod(start, end);
        } else if (organizer != null) {
            return EventApiClient.getInstance().findByOrganizer(organizer.getId());
        } else if (venue != null) {
            return EventApiClient.getInstance().findByVenue(venue.getId());
        } else {
            return EventApiClient.getInstance().findAll();
        }
    }

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