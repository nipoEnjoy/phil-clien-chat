package com.npopov.philharmonic.client.ui.dialog;

import com.npopov.philharmonic.client.api.OrganizerApiClient;
import com.npopov.philharmonic.client.api.VenueApiClient;
import com.npopov.philharmonic.client.model.EventModel;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDialog extends Dialog<Map<String, Object>> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final TextField titleField;
    private final ComboBox<String> eventTypeCombo;
    private final ComboBox<VenueModel> venueCombo;
    private final ComboBox<OrganizerModel> organizerCombo;
    private final TextArea descriptionArea;

    private final DatePicker startDatePicker;
    private final Spinner<Integer> startHourSpinner;
    private final Spinner<Integer> startMinuteSpinner;

    private final DatePicker endDatePicker;
    private final Spinner<Integer> endHourSpinner;
    private final Spinner<Integer> endMinuteSpinner;

    private final VBox dynamicFieldsContainer;
    private final GridPane concertPane;
    private final GridPane competitionPane;

    // данные для ComboBox (кешируются после загрузки)
    private List<VenueModel> allVenues;
    private List<OrganizerModel> allOrganizers;

    public EventDialog(EventModel existing) {
        setTitle(existing == null ? "Новое мероприятие" : "Редактировать мероприятие");
        setHeaderText(null);

        // Базовые поля
        titleField = createTextField(existing != null ? existing.getTitle() : "");
        eventTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "CONCERT", "COMPETITION", "SOLO", "FESTIVAL", "OTHER"));
        eventTypeCombo.setPrefWidth(160);
        if (existing != null && existing.getEventType() != null) {
            eventTypeCombo.setValue(existing.getEventType());
        } else {
            eventTypeCombo.setValue("CONCERT");
        }

        // Создаём ComboBox для площадок и организаторов
        venueCombo = new ComboBox<>();
        organizerCombo = new ComboBox<>();
        venueCombo.setPrefWidth(220);
        organizerCombo.setPrefWidth(220);
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

        // Панели дополнительных полей
        concertPane = createConcertPane(existing);
        competitionPane = createCompetitionPane(existing);
        dynamicFieldsContainer = new VBox(8);

        // Основной layout
        GridPane baseGrid = new GridPane();
        baseGrid.setHgap(10);
        baseGrid.setVgap(8);
        baseGrid.setPadding(new Insets(16));
        baseGrid.addRow(0, label("Название *"), titleField);
        baseGrid.addRow(1, label("Тип"), eventTypeCombo);
        baseGrid.addRow(2, label("Площадка *"), venueCombo);
        baseGrid.addRow(3, label("Организатор"), organizerCombo);

        HBox startBox = new HBox(6, startDatePicker, startHourSpinner, new Label(":"), startMinuteSpinner);
        baseGrid.addRow(4, label("Начало *"), startBox);
        HBox endBox = new HBox(6, endDatePicker, endHourSpinner, new Label(":"), endMinuteSpinner);
        baseGrid.addRow(5, label("Конец"), endBox);
        baseGrid.addRow(6, label("Описание"), descriptionArea);
        baseGrid.addRow(7, label("Детали"), dynamicFieldsContainer);

        getDialogPane().setContent(baseGrid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        setOnShowing(ev -> {
            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            stage.setResizable(true);
            stage.setMinWidth(400);
            stage.setMinHeight(300);
        });

        // Загружаем данные для ComboBox асинхронно
        loadVenuesAsync(existing);
        loadOrganizersAsync(existing);

        // Логика переключения дополнительных полей
        eventTypeCombo.valueProperty().addListener((obs, old, newType) -> {
            updateDynamicFields(newType);
            getDialogPane().requestLayout();
            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            if (stage != null) {
                stage.sizeToScene();
            }
        });
        updateDynamicFields(eventTypeCombo.getValue());

        // Валидация
        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        Runnable validate = () -> {
            boolean baseOk = !titleField.getText().trim().isEmpty()
                    && venueCombo.getSelectionModel().getSelectedItem() != null;
            LocalDateTime start = toLocalDateTime(startDatePicker, startHourSpinner, startMinuteSpinner);
            if (start == null) baseOk = false;
            else if (start.isBefore(LocalDateTime.now())) baseOk = false;
            LocalDateTime end = toLocalDateTime(endDatePicker, endHourSpinner, endMinuteSpinner);
            if (end != null && !end.isAfter(start)) baseOk = false;
            okButton.setDisable(!baseOk);
        };
        titleField.textProperty().addListener((o,ov,nv) -> validate.run());
        venueCombo.getSelectionModel().selectedItemProperty().addListener((o,ov,nv) -> validate.run());
        startDatePicker.valueProperty().addListener((o,ov,nv) -> validate.run());
        startHourSpinner.valueProperty().addListener((o,ov,nv) -> validate.run());
        startMinuteSpinner.valueProperty().addListener((o,ov,nv) -> validate.run());
        endDatePicker.valueProperty().addListener((o,ov,nv) -> validate.run());
        endHourSpinner.valueProperty().addListener((o,ov,nv) -> validate.run());
        endMinuteSpinner.valueProperty().addListener((o,ov,nv) -> validate.run());
        validate.run();

        setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> data = new HashMap<>();
            data.put("title", titleField.getText().trim());
            data.put("eventType", eventTypeCombo.getValue());

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

            String type = eventTypeCombo.getValue();
            switch (type) {
                case "CONCERT" -> data.put("program", getTextFromField(concertPane, "program"));
                case "COMPETITION" -> {
                    data.put("competitionType", getTextFromField(competitionPane, "competitionType"));
                    data.put("rules", getTextFromField(competitionPane, "rules"));
                    data.put("juryInfo", getTextFromField(competitionPane, "juryInfo"));
                }
                // SOLO, FESTIVAL, OTHER – без дополнительных полей
            }
            return data;
        });
    }

    private void configureComboBoxes() {
        venueCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(VenueModel v) {
                return v == null ? "" : v.getName() + " (id=" + v.getId() + ")";
            }
            @Override
            public VenueModel fromString(String s) { return null; }
        });
        organizerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(OrganizerModel o) {
                return o == null ? "" : o.getName() + " (id=" + o.getId() + ")";
            }
            @Override
            public OrganizerModel fromString(String s) { return null; }
        });
        // начальное состояние: загружается
        venueCombo.setDisable(true);
        organizerCombo.setDisable(true);
    }

    private void loadVenuesAsync(EventModel existing) {
        Task<List<VenueModel>> task = new Task<>() {
            @Override
            protected List<VenueModel> call() {
                return VenueApiClient.getInstance().findAll();
            }
        };
        task.setOnSucceeded(e -> {
            allVenues = task.getValue();
            Platform.runLater(() -> {
                venueCombo.setItems(FXCollections.observableArrayList(allVenues));
                venueCombo.setDisable(false);
                if (existing != null && existing.getVenueId() != null) {
                    VenueModel preselected = allVenues.stream()
                            .filter(v -> v.getId().equals(existing.getVenueId()))
                            .findFirst()
                            .orElse(null);
                    venueCombo.getSelectionModel().select(preselected);
                }
            });
        });
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                venueCombo.setDisable(false);
                venueCombo.setPromptText("Ошибка загрузки");
                showError("Не удалось загрузить площадки: " + task.getException().getMessage());
            });
        });
        new Thread(task).start();
    }

    private void loadOrganizersAsync(EventModel existing) {
        Task<List<OrganizerModel>> task = new Task<>() {
            @Override
            protected List<OrganizerModel> call() {
                return OrganizerApiClient.getInstance().findAll();
            }
        };
        task.setOnSucceeded(e -> {
            allOrganizers = task.getValue();
            Platform.runLater(() -> {
                organizerCombo.setItems(FXCollections.observableArrayList(allOrganizers));
                organizerCombo.setDisable(false);
                if (existing != null && existing.getOrganizerId() != null) {
                    OrganizerModel preselected = allOrganizers.stream()
                            .filter(o -> o.getId().equals(existing.getOrganizerId()))
                            .findFirst()
                            .orElse(null);
                    organizerCombo.getSelectionModel().select(preselected);
                }
            });
        });
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                organizerCombo.setDisable(false);
                organizerCombo.setPromptText("Ошибка загрузки");
                showError("Не удалось загрузить организаторов: " + task.getException().getMessage());
            });
        });
        new Thread(task).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private GridPane createConcertPane(EventModel ex) {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        TextField programField = createTextField(ex != null ? ex.getProgram() : null);
        gp.addRow(0, label("Программа"), programField);
        programField.setId("program");
        return gp;
    }

    private GridPane createCompetitionPane(EventModel ex) {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        TextField typeField = createTextField(ex != null ? ex.getCompetitionType() : null);
        TextArea rulesArea = new TextArea();
        rulesArea.setPrefRowCount(2);
        if (ex != null) rulesArea.setText(ex.getRules());
        TextArea juryArea = new TextArea();
        juryArea.setPrefRowCount(2);
        if (ex != null) juryArea.setText(ex.getJuryInfo());

        gp.addRow(0, label("Тип конкурса"), typeField);
        gp.addRow(1, label("Правила"), rulesArea);
        gp.addRow(2, label("Жюри"), juryArea);
        typeField.setId("competitionType");
        rulesArea.setId("rules");
        juryArea.setId("juryInfo");
        return gp;
    }

    private void updateDynamicFields(String type) {
        dynamicFieldsContainer.getChildren().clear();
        switch (type) {
            case "CONCERT" -> dynamicFieldsContainer.getChildren().add(concertPane);
            case "COMPETITION" -> dynamicFieldsContainer.getChildren().add(competitionPane);
            default -> { /* без дополнительных полей */ }
        }
    }

    private TextField createTextField(String val) {
        TextField tf = new TextField(val == null ? "" : val);
        tf.getStyleClass().add("text-field-lg");
        return tf;
    }

    private Spinner<Integer> createTimeSpinner() {
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory vf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
        vf.setConverter(new StringConverter<>() {
            @Override public String toString(Integer value) {
                return String.format("%02d", value == null ? 0 : value);
            }
            @Override public Integer fromString(String s) {
                try { return Integer.parseInt(s); } catch (Exception e) { return vf.getValue(); }
            }
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

    private Long parseLong(String s) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return null; }
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private String getTextFromField(GridPane gp, String fieldId) {
        Node node = gp.lookup("#" + fieldId);
        if (node instanceof TextField tf) {
            String val = tf.getText().trim();
            return val.isEmpty() ? null : val;
        } else if (node instanceof TextArea ta) {
            String val = ta.getText().trim();
            return val.isEmpty() ? null : val;
        }
        return null;
    }
}