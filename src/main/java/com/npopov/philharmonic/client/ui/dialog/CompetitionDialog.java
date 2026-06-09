package com.npopov.philharmonic.client.ui.dialog;

import com.npopov.philharmonic.client.api.OrganizerApiClient;
import com.npopov.philharmonic.client.api.VenueApiClient;
import com.npopov.philharmonic.client.model.CompetitionModel;
import com.npopov.philharmonic.client.model.OrganizerModel;
import com.npopov.philharmonic.client.model.VenueModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompetitionDialog extends Dialog<Map<String, Object>> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final TextField titleField;
    private final ComboBox<VenueModel> venueCombo;
    private final ComboBox<OrganizerModel> organizerCombo;
    private final TextArea descriptionArea;
    private final DatePicker startDatePicker;
    private final Spinner<Integer> startHourSpinner;
    private final Spinner<Integer> startMinuteSpinner;
    private final DatePicker endDatePicker;
    private final Spinner<Integer> endHourSpinner;
    private final Spinner<Integer> endMinuteSpinner;
    private final TextField competitionTypeField;
    private final TextArea rulesArea;
    private final TextArea juryInfoArea;

    private List<VenueModel> allVenues;
    private List<OrganizerModel> allOrganizers;

    public CompetitionDialog(CompetitionModel existing) {
        setTitle(existing == null ? "Новый конкурс" : "Редактировать конкурс");
        setHeaderText(null);

        // Основные поля
        titleField = createTextField(existing != null ? existing.getTitle() : "");
        venueCombo = new ComboBox<>();
        organizerCombo = new ComboBox<>();
        configureComboBoxes();

        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        if (existing != null) descriptionArea.setText(existing.getDescription());

        // Дата/время
        startDatePicker = new DatePicker();
        startHourSpinner = createTimeSpinner();
        startMinuteSpinner = createTimeSpinner();
        endDatePicker = new DatePicker();
        endHourSpinner = createTimeSpinner();
        endMinuteSpinner = createTimeSpinner();

        if (existing != null && existing.getStartDatetime() != null) {
            startDatePicker.setValue(existing.getStartDatetime().toLocalDate());
            startHourSpinner.getValueFactory().setValue(existing.getStartDatetime().getHour());
            startMinuteSpinner.getValueFactory().setValue(existing.getStartDatetime().getMinute());
        }
        if (existing != null && existing.getEndDatetime() != null) {
            endDatePicker.setValue(existing.getEndDatetime().toLocalDate());
            endHourSpinner.getValueFactory().setValue(existing.getEndDatetime().getHour());
            endMinuteSpinner.getValueFactory().setValue(existing.getEndDatetime().getMinute());
        }

        // Поля конкурса
        competitionTypeField = createTextField(existing != null ? existing.getCompetitionType() : "");
        rulesArea = new TextArea();
        rulesArea.setPrefRowCount(2);
        if (existing != null) rulesArea.setText(existing.getRules());
        juryInfoArea = new TextArea();
        juryInfoArea.setPrefRowCount(2);
        if (existing != null) juryInfoArea.setText(existing.getJuryInfo());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, label("Название *"), titleField);
        grid.addRow(1, label("Площадка *"), venueCombo);
        grid.addRow(2, label("Организатор"), organizerCombo);
        HBox startBox = new HBox(6, startDatePicker, startHourSpinner, new Label(":"), startMinuteSpinner);
        grid.addRow(3, label("Начало *"), startBox);
        HBox endBox = new HBox(6, endDatePicker, endHourSpinner, new Label(":"), endMinuteSpinner);
        grid.addRow(4, label("Конец"), endBox);
        grid.addRow(5, label("Описание"), descriptionArea);
        grid.addRow(6, label("Тип конкурса"), competitionTypeField);
        grid.addRow(7, label("Правила"), rulesArea);
        grid.addRow(8, label("Жюри"), juryInfoArea);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        setOnShowing(ev -> {
            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            stage.setResizable(true);
            stage.setMinWidth(500);
            stage.setMinHeight(500);
        });

        // Загрузка данных для комбобоксов
        loadVenuesAsync(existing);
        loadOrganizersAsync(existing);

        // Валидация
        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        Runnable validate = () -> {
            boolean ok = !titleField.getText().trim().isEmpty()
                    && venueCombo.getSelectionModel().getSelectedItem() != null
                    && startDatePicker.getValue() != null;
            LocalDateTime start = toLocalDateTime(startDatePicker, startHourSpinner, startMinuteSpinner);
            if (start != null && start.isBefore(LocalDateTime.now())) ok = false;
            LocalDateTime end = toLocalDateTime(endDatePicker, endHourSpinner, endMinuteSpinner);
            if (end != null && !end.isAfter(start)) ok = false;
            okButton.setDisable(!ok);
        };
        titleField.textProperty().addListener((o, ov, nv) -> validate.run());
        venueCombo.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> validate.run());
        startDatePicker.valueProperty().addListener((o, ov, nv) -> validate.run());
        startHourSpinner.valueProperty().addListener((o, ov, nv) -> validate.run());
        startMinuteSpinner.valueProperty().addListener((o, ov, nv) -> validate.run());
        endDatePicker.valueProperty().addListener((o, ov, nv) -> validate.run());
        endHourSpinner.valueProperty().addListener((o, ov, nv) -> validate.run());
        endMinuteSpinner.valueProperty().addListener((o, ov, nv) -> validate.run());
        validate.run();

        setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> data = new HashMap<>();
            data.put("title", titleField.getText().trim());
            data.put("eventType", "COMPETITION");

            VenueModel selectedVenue = venueCombo.getSelectionModel().getSelectedItem();
            if (selectedVenue == null) return null;
            data.put("venueId", selectedVenue.getId());

            OrganizerModel selectedOrganizer = organizerCombo.getSelectionModel().getSelectedItem();
            data.put("organizerId", selectedOrganizer != null ? selectedOrganizer.getId() : null);

            LocalDateTime start = toLocalDateTime(startDatePicker, startHourSpinner, startMinuteSpinner);
            if (start != null) data.put("startDatetime", start.format(ISO));
            LocalDateTime end = toLocalDateTime(endDatePicker, endHourSpinner, endMinuteSpinner);
            if (end != null) data.put("endDatetime", end.format(ISO));
            data.put("description", descriptionArea.getText().trim());
            data.put("competitionType", competitionTypeField.getText().trim());
            data.put("rules", rulesArea.getText().trim());
            data.put("juryInfo", juryInfoArea.getText().trim());
            return data;
        });
    }

    private void configureComboBoxes() {
        venueCombo.setConverter(new StringConverter<>() {
            @Override public String toString(VenueModel v) { return v == null ? "" : v.getName() + " (id=" + v.getId() + ")"; }
            @Override public VenueModel fromString(String s) { return null; }
        });
        organizerCombo.setConverter(new StringConverter<>() {
            @Override public String toString(OrganizerModel o) { return o == null ? "" : o.getName() + " (id=" + o.getId() + ")"; }
            @Override public OrganizerModel fromString(String s) { return null; }
        });
        venueCombo.setDisable(true);
        organizerCombo.setDisable(true);
    }

    private void loadVenuesAsync(CompetitionModel existing) {
        Task<List<VenueModel>> task = new Task<>() {
            @Override protected List<VenueModel> call() { return VenueApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            allVenues = task.getValue();
            Platform.runLater(() -> {
                venueCombo.setItems(FXCollections.observableArrayList(allVenues));
                venueCombo.setDisable(false);
                if (existing != null && existing.getVenueId() != null) {
                    venueCombo.getSelectionModel().select(
                            allVenues.stream().filter(v -> v.getId().equals(existing.getVenueId())).findFirst().orElse(null));
                }
            });
        });
        new Thread(task).start();
    }

    private void loadOrganizersAsync(CompetitionModel existing) {
        Task<List<OrganizerModel>> task = new Task<>() {
            @Override protected List<OrganizerModel> call() { return OrganizerApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            allOrganizers = task.getValue();
            Platform.runLater(() -> {
                organizerCombo.setItems(FXCollections.observableArrayList(allOrganizers));
                organizerCombo.setDisable(false);
                if (existing != null && existing.getOrganizerId() != null) {
                    organizerCombo.getSelectionModel().select(
                            allOrganizers.stream().filter(o -> o.getId().equals(existing.getOrganizerId())).findFirst().orElse(null));
                }
            });
        });
        new Thread(task).start();
    }

    private TextField createTextField(String val) {
        TextField tf = new TextField(val == null ? "" : val);
        tf.getStyleClass().add("text-field-lg");
        return tf;
    }

    private Spinner<Integer> createTimeSpinner() {
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
        vf.setConverter(new StringConverter<>() {
            @Override public String toString(Integer value) { return String.format("%02d", value); }
            @Override public Integer fromString(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return vf.getValue(); } }
        });
        spinner.setValueFactory(vf);
        spinner.setEditable(true);
        spinner.setPrefWidth(60);
        return spinner;
    }

    private LocalDateTime toLocalDateTime(DatePicker dp, Spinner<Integer> h, Spinner<Integer> m) {
        LocalDate d = dp.getValue();
        if (d == null) return null;
        return LocalDateTime.of(d, LocalTime.of(h.getValue(), m.getValue()));
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }
}