package com.npopov.philharmonic.client.ui.dialog;

import com.npopov.philharmonic.client.api.ArtistApiClient;
import com.npopov.philharmonic.client.api.CompetitionApiClient;
import com.npopov.philharmonic.client.model.ArtistModel;
import com.npopov.philharmonic.client.model.CompetitionModel;
import com.npopov.philharmonic.client.model.CompetitionResultModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompetitionResultDialog extends Dialog<Map<String, Object>> {

    private final ComboBox<CompetitionModel> competitionCombo;
    private final ComboBox<ArtistModel> artistCombo;
    private final TextField placeField;
    private final TextField awardField;

    private List<CompetitionModel> allCompetitions;
    private List<ArtistModel> allArtists;

    public CompetitionResultDialog(CompetitionResultModel existing) {
        setTitle(existing == null ? "Новый результат" : "Редактировать результат");
        setHeaderText(null);

        competitionCombo = new ComboBox<>();
        artistCombo = new ComboBox<>();
        configureComboBoxes();

        placeField = new TextField();
        placeField.setPromptText("Место (число)");
        awardField = new TextField();
        awardField.setPromptText("Награда");

        if (existing != null) {
            placeField.setText(existing.getPlace() != null ? existing.getPlace().toString() : "");
            awardField.setText(existing.getAward());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Конкурс *"), competitionCombo);
        grid.addRow(1, new Label("Артист *"), artistCombo);
        grid.addRow(2, new Label("Место"), placeField);
        grid.addRow(3, new Label("Награда"), awardField);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        // Загрузка данных
        loadCompetitionsAsync(existing);
        loadArtistsAsync(existing);

        // Валидация
        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        Runnable validate = () -> {
            boolean ok = competitionCombo.getSelectionModel().getSelectedItem() != null
                    && artistCombo.getSelectionModel().getSelectedItem() != null;
            okButton.setDisable(!ok);
        };
        competitionCombo.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> validate.run());
        artistCombo.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> validate.run());
        validate.run();

        setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> data = new HashMap<>();
            data.put("competitionId", competitionCombo.getSelectionModel().getSelectedItem().getId());
            data.put("artistId", artistCombo.getSelectionModel().getSelectedItem().getId());
            try {
                Integer place = placeField.getText().isEmpty() ? null : Integer.parseInt(placeField.getText());
                data.put("place", place);
            } catch (NumberFormatException e) { data.put("place", null); }
            data.put("award", awardField.getText().trim());
            return data;
        });
    }

    private void configureComboBoxes() {
        competitionCombo.setConverter(new StringConverter<>() {
            @Override public String toString(CompetitionModel c) { return c == null ? "" : c.getTitle() + " (id=" + c.getId() + ")"; }
            @Override public CompetitionModel fromString(String s) { return null; }
        });
        artistCombo.setConverter(new StringConverter<>() {
            @Override public String toString(ArtistModel a) { return a == null ? "" : a.getFullName(); }
            @Override public ArtistModel fromString(String s) { return null; }
        });
        competitionCombo.setDisable(true);
        artistCombo.setDisable(true);
    }

    private void loadCompetitionsAsync(CompetitionResultModel existing) {
        Task<List<CompetitionModel>> task = new Task<>() {
            @Override protected List<CompetitionModel> call() { return CompetitionApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            allCompetitions = task.getValue();
            Platform.runLater(() -> {
                competitionCombo.setItems(FXCollections.observableArrayList(allCompetitions));
                competitionCombo.setDisable(false);
                if (existing != null && existing.getCompetitionId() != null) {
                    competitionCombo.getSelectionModel().select(
                            allCompetitions.stream().filter(c -> c.getId().equals(existing.getCompetitionId())).findFirst().orElse(null));
                }
            });
        });
        new Thread(task).start();
    }

    private void loadArtistsAsync(CompetitionResultModel existing) {
        Task<List<ArtistModel>> task = new Task<>() {
            @Override protected List<ArtistModel> call() { return ArtistApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            allArtists = task.getValue();
            Platform.runLater(() -> {
                artistCombo.setItems(FXCollections.observableArrayList(allArtists));
                artistCombo.setDisable(false);
                if (existing != null && existing.getArtistId() != null) {
                    artistCombo.getSelectionModel().select(
                            allArtists.stream().filter(a -> a.getId().equals(existing.getArtistId())).findFirst().orElse(null));
                }
            });
        });
        new Thread(task).start();
    }
}