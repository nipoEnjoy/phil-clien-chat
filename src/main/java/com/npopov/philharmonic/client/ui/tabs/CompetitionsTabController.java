package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.CompetitionApiClient;
import com.npopov.philharmonic.client.model.CompetitionModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import com.npopov.philharmonic.client.ui.dialog.CompetitionDialog;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

public class CompetitionsTabController extends BaseTabController<CompetitionModel> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    protected void buildColumns() {
        TableColumn<CompetitionModel, Long> idCol = col("ID", 60, "id");
        TableColumn<CompetitionModel, String> titleCol = col("Название", 200, "title");
        TableColumn<CompetitionModel, String> typeCol = col("Тип конкурса", 150, "competitionType");
        TableColumn<CompetitionModel, String> venueCol = col("Площадка", 150, "venueName");
        TableColumn<CompetitionModel, String> startCol = col("Начало", 150, "startDatetime");
        TableColumn<CompetitionModel, String> endCol = col("Конец", 150, "endDatetime");

        // настраиваем отображение дат
        startCol.setCellValueFactory(cd -> {
            var date = cd.getValue().getStartDatetime();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(FMT) : "");
        });
        endCol.setCellValueFactory(cd -> {
            var date = cd.getValue().getEndDatetime();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(FMT) : "");
        });

        tableView.getColumns().addAll(idCol, titleCol, typeCol, venueCol, startCol, endCol);
    }

    @Override
    protected List<CompetitionModel> loadData() {
        return CompetitionApiClient.getInstance().findAll();
    }

    @Override
    protected Predicate<CompetitionModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return c -> true;
        String lower = q.toLowerCase();
        return c -> str(c.getTitle()).toLowerCase().contains(lower) ||
                str(c.getCompetitionType()).toLowerCase().contains(lower) ||
                str(c.getVenueName()).toLowerCase().contains(lower);
    }

    @Override
    protected void showCreateDialog() {
        CompetitionDialog dialog = new CompetitionDialog(null);
        dialog.showAndWait().ifPresent(body -> {
            try {
                CompetitionApiClient.getInstance().create(body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(CompetitionModel item) {
        CompetitionDialog dialog = new CompetitionDialog(item);
        dialog.showAndWait().ifPresent(body -> {
            try {
                CompetitionApiClient.getInstance().update(item.getId(), body);
                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void deleteSelected(CompetitionModel item) {
        CompetitionApiClient.getInstance().delete(item.getId());
    }

    private <V> TableColumn<CompetitionModel, V> col(String title, double width, String prop) {
        TableColumn<CompetitionModel, V> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }
}