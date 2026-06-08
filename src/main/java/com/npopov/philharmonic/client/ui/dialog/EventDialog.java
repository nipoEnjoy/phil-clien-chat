package com.npopov.philharmonic.client.ui.dialog;

import com.npopov.philharmonic.client.model.EventModel;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;

import java.time.*;

public class EventDialog extends Dialog<Map<String, Object>> {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public EventDialog(EventModel ex) {
        setTitle(ex == null ? "Новое мероприятие" : "Редактировать мероприятие");
        setHeaderText(null);

        TextField title       = field(ex != null ? str(ex.getTitle())       : "");
        ComboBox<String> eventType = new ComboBox<>(FXCollections.observableArrayList(
                "CONCERT",
                "COMPETITION",
                "RECITAL",
                "FESTIVAL",
                "OTHER"));
        eventType.setEditable(false);
        eventType.setPrefWidth(160);
        if (ex != null && ex.getEventType() != null) eventType.setValue(ex.getEventType());

        TextField venueId     = field(ex != null ? str(ex.getVenueId())     : "");
        TextField organizerId = field(ex != null ? str(ex.getOrganizerId()) : "");
        TextField description = field(ex != null ? str(ex.getDescription()) : "");

        // Start controls
        DatePicker startDate = new DatePicker();
        Spinner<Integer> startHour = new Spinner<>();
        Spinner<Integer> startMin  = new Spinner<>();
        initTimeSpinner(startHour, 0, 23);
        initTimeSpinner(startMin, 0, 59);

        // End controls (optional)
        DatePicker endDate = new DatePicker();
        Spinner<Integer> endHour = new Spinner<>();
        Spinner<Integer> endMin  = new Spinner<>();
        initTimeSpinner(endHour, 0, 23);
        initTimeSpinner(endMin, 0, 59);

        // Pre-fill if editing
        if (ex != null && ex.getStartDatetime() != null) {
            LocalDateTime s = ex.getStartDatetime();
            startDate.setValue(s.toLocalDate());
            startHour.getValueFactory().setValue(s.getHour());
            startMin.getValueFactory().setValue(s.getMinute());
        }
        if (ex != null && ex.getEndDatetime() != null) {
            LocalDateTime e = ex.getEndDatetime();
            endDate.setValue(e.toLocalDate());
            endHour.getValueFactory().setValue(e.getHour());
            endMin.getValueFactory().setValue(e.getMinute());
        }

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));

        grid.addRow(0, label("Название *"), title);
        grid.addRow(1, label("Тип"), eventType);
        grid.addRow(2, label("ID площадки *"), venueId);
        grid.addRow(3, label("ID организатора"), organizerId);

        HBox startBox = new HBox(6, startDate, startHour, new Label(":"), startMin);
        grid.addRow(4, label("Начало *"), startBox);

        HBox endBox = new HBox(6, endDate, endHour, new Label(":"), endMin);
        grid.addRow(5, label("Конец"), endBox);

        grid.addRow(6, label("Описание"), description);
        title.setPrefWidth(240);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        Button okBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(true);

        // Validation runnable
        Runnable validate = () -> {
            boolean baseFields = !title.getText().trim().isEmpty() && !venueId.getText().trim().isEmpty();
            LocalDateTime start = toLocalDateTime(startDate, startHour, startMin);
            if (start == null) { okBtn.setDisable(true); return; }
            boolean startNotPast = !start.isBefore(LocalDateTime.now());
            LocalDateTime end = toLocalDateTime(endDate, endHour, endMin);
            boolean endAfterStart = (end == null) || end.isAfter(start);
            okBtn.setDisable(!(baseFields && startNotPast && endAfterStart));
        };

        // Listeners
        title.textProperty().addListener((o,ov,nv) -> validate.run());
        venueId.textProperty().addListener((o,ov,nv) -> validate.run());
        startDate.valueProperty().addListener((o,ov,nv) -> validate.run());
        startHour.valueProperty().addListener((o,ov,nv) -> validate.run());
        startMin.valueProperty().addListener((o,ov,nv) -> validate.run());
        endDate.valueProperty().addListener((o,ov,nv) -> validate.run());
        endHour.valueProperty().addListener((o,ov,nv) -> validate.run());
        endMin.valueProperty().addListener((o,ov,nv) -> validate.run());

        validate.run();

        setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String,Object> map = new HashMap<>();
            map.put("title", title.getText().trim());
            String et = eventType.getValue();
            map.put("eventType", et == null || et.trim().isEmpty() ? null : et);
            map.put("venueId", parseLong(venueId.getText()));
            map.put("organizerId", parseLong(organizerId.getText()));
            LocalDateTime start = toLocalDateTime(startDate, startHour, startMin);
            map.put("startDatetime", start == null ? null : start.format(ISO));
            LocalDateTime end = toLocalDateTime(endDate, endHour, endMin);
            map.put("endDatetime", end == null ? null : end.format(ISO));
            map.put("description", description.getText().trim());
            return map;
        });
    }

    private static void initTimeSpinner(Spinner<Integer> spinner, int min, int max) {
        SpinnerValueFactory.IntegerSpinnerValueFactory vf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, min);
        spinner.setValueFactory(vf);
        vf.setConverter(new StringConverter<>() {
            @Override public String toString(Integer value) {
                return String.format("%02d", value == null ? 0 : value);
            }
            @Override public Integer fromString(String s) {
                try { return Integer.parseInt(s); } catch (Exception e) { return vf.getValue(); }
            }
        });
        spinner.setEditable(true);
        spinner.setPrefWidth(60);
    }

    private static LocalDateTime toLocalDateTime(DatePicker dp, Spinner<Integer> h, Spinner<Integer> m) {
        LocalDate d = dp.getValue();
        if (d == null) return null;
        return LocalDateTime.of(d, LocalTime.of(h.getValue(), m.getValue()));
    }

    private static Long parseLong(String s) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return null; }
    }
    private static String str(Object o) { return o == null ? "" : o.toString(); }
    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.getStyleClass().add("text-field-lg");
        return tf;
    }
    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }
}
