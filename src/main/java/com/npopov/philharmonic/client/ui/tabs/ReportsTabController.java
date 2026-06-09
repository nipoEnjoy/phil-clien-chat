package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.ReportApiClient;
import com.npopov.philharmonic.client.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsTabController {  // Не наследуем BaseTabController

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button btnGenerate;
    @FXML private Button btnRefresh;
    @FXML private TableView tableView;   // raw type – работает с любыми колонками
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private Label statusLabel;
    // @FXML private HBox filterBar; // если не используется, можно удалить

    @FXML
    public void initialize() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Артисты без конкурсов за период",
                "Организаторы и число концертов за период",
                "Сооружения и даты мероприятий за период"
        ));
        reportTypeCombo.getSelectionModel().selectFirst();
        startDatePicker.setValue(LocalDate.now().minusMonths(6));
        endDatePicker.setValue(LocalDate.now());

        btnGenerate.setOnAction(e -> generateReport());
        btnRefresh.setOnAction(e -> generateReport());

        generateReport(); // первая загрузка
    }

    @FXML
    private void generateReport() {
        LocalDateTime start = startDatePicker.getValue().atStartOfDay();
        LocalDateTime end = endDatePicker.getValue().atTime(23, 59, 59);
        String selected = reportTypeCombo.getValue();
        if (selected == null) return;

        setLoading(true);
        setStatus("Загрузка...");

        Task<List<?>> task = new Task<>() {
            @Override protected List<?> call() throws Exception {
                if (selected.startsWith("Артисты без конкурсов")) {
                    return ReportApiClient.getInstance().getArtistsNoCompetitions(start, end);
                } else if (selected.startsWith("Организаторы")) {
                    return ReportApiClient.getInstance().getOrganizerStats(start, end);
                } else {
                    return ReportApiClient.getInstance().getVenuesWithEvents(start, end);
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<?> data = task.getValue();
            buildColumnsForSelectedReport(selected);
            ObservableList<?> items = FXCollections.observableArrayList(data);
            tableView.setItems(items);
            setLoading(false);
            setStatus("Записей: " + items.size());
        });

        task.setOnFailed(e -> {
            setLoading(false);
            setStatus("Ошибка: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void buildColumnsForSelectedReport(String reportType) {
        tableView.getColumns().clear();
        if (reportType.startsWith("Артисты без конкурсов")) {
            TableColumn<ArtistModel, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            TableColumn<ArtistModel, String> firstCol = new TableColumn<>("Имя");
            firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            TableColumn<ArtistModel, String> lastCol = new TableColumn<>("Фамилия");
            lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            TableColumn<ArtistModel, String> stageCol = new TableColumn<>("Псевдоним");
            stageCol.setCellValueFactory(new PropertyValueFactory<>("stageName"));
            // Добавляем колонки напрямую – raw tableView позволяет
            tableView.getColumns().addAll(idCol, firstCol, lastCol, stageCol);
        } else if (reportType.startsWith("Организаторы")) {
            TableColumn<OrganizerStat, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            TableColumn<OrganizerStat, String> nameCol = new TableColumn<>("Название");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            TableColumn<OrganizerStat, Long> countCol = new TableColumn<>("Концертов");
            countCol.setCellValueFactory(new PropertyValueFactory<>("eventsCount"));
            tableView.getColumns().addAll(idCol, nameCol, countCol);
        } else {
            TableColumn<VenueWithEvent, Long> venueIdCol = new TableColumn<>("ID площадки");
            venueIdCol.setCellValueFactory(new PropertyValueFactory<>("venueId"));
            TableColumn<VenueWithEvent, String> venueNameCol = new TableColumn<>("Площадка");
            venueNameCol.setCellValueFactory(new PropertyValueFactory<>("venueName"));
            TableColumn<VenueWithEvent, Long> eventIdCol = new TableColumn<>("ID события");
            eventIdCol.setCellValueFactory(new PropertyValueFactory<>("eventId"));
            TableColumn<VenueWithEvent, String> eventTitleCol = new TableColumn<>("Мероприятие");
            eventTitleCol.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
            TableColumn<VenueWithEvent, String> dateCol = new TableColumn<>("Дата");
            dateCol.setCellValueFactory(cd -> {
                LocalDateTime dt = cd.getValue().getEventDate();
                return new javafx.beans.property.SimpleStringProperty(
                        dt != null ? dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : ""
                );
            });
            tableView.getColumns().addAll(venueIdCol, venueNameCol, eventIdCol, eventTitleCol, dateCol);
        }
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            loadingSpinner.setVisible(loading);
            btnGenerate.setDisable(loading);
            btnRefresh.setDisable(loading);
            reportTypeCombo.setDisable(loading);
            startDatePicker.setDisable(loading);
            endDatePicker.setDisable(loading);
        });
    }

    private void setStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }
}