package com.npopov.philharmonic.client.ui.dialog;

import com.npopov.philharmonic.client.model.VenueModel;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class VenueDialog extends Dialog<Map<String, Object>> {

    private final TextField nameField;
    private final TextField cityField;
    private final TextField addressField;
    private final TextField descriptionField;
    private final ComboBox<String> typeCombo;

    // Поля для Theatre
    private final TextField capacityField;
    private final TextField stageWidthField;
    private final TextField stageDepthField;

    // Поля для Cinema
    private final TextField screenWidthField;
    private final TextField screenHeightField;
    private final TextField screenDiagonalField;
    private final TextField screenAspectRatioField;

    // Поля для ConcertVenue
    private final TextField stageTypeField;
    private final CheckBox hasSoundSystemCheck;

    // Поля для CulturalCentre
    private final TextField communityRoomsCountField;

    // Поля для VarietyStage
    private final TextField genreFocusField;

    private final VBox dynamicFieldsContainer;

    public VenueDialog(VenueModel existing) {
        setTitle(existing == null ? "Новая площадка" : "Редактировать площадку");
        setHeaderText(null);

        // Базовые поля
        nameField = createTextField(existing != null ? existing.getName() : "");
        cityField = createTextField(existing != null ? existing.getCity() : "");
        addressField = createTextField(existing != null ? existing.getAddress() : "");
        descriptionField = createTextField(existing != null ? existing.getDescription() : "");

        typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "THEATRE", "CINEMA", "CONCERT_VENUE", "VARIETY_STAGE", "CULTURAL_CENTRE"));
        typeCombo.setPrefWidth(200);
        typeCombo.setValue(existing != null && existing.getVenueType() != null ? existing.getVenueType() : "THEATRE");

        // Создаём специфичные поля (инициализируем со значениями из existing)
        capacityField = createIntField(existing != null ? existing.getCapacity() : null);
        stageWidthField = createIntField(existing != null ? existing.getStageWidthMm() : null);
        stageDepthField = createIntField(existing != null ? existing.getStageDepthMm() : null);

        screenWidthField = createIntField(existing != null ? existing.getScreenWidthMm() : null);
        screenHeightField = createIntField(existing != null ? existing.getScreenHeightMm() : null);
        screenDiagonalField = createIntField(existing != null ? existing.getScreenDiagonalMm() : null);
        screenAspectRatioField = createTextField(existing != null ? existing.getScreenAspectRatio() : null);

        stageTypeField = createTextField(existing != null ? existing.getStageType() : null);
        hasSoundSystemCheck = new CheckBox("Есть звуковая система");
        if (existing != null && Boolean.TRUE.equals(existing.getHasSoundSystem())) {
            hasSoundSystemCheck.setSelected(true);
        }

        communityRoomsCountField = createIntField(existing != null ? existing.getCommunityRoomsCount() : null);

        genreFocusField = createTextField(existing != null ? existing.getGenreFocus() : null);

        // Панели для каждого типа (просто группируем поля)
        GridPane theatrePane = createTheatrePane();
        GridPane cinemaPane = createCinemaPane();
        GridPane concertVenuePane = createConcertVenuePane();
        GridPane culturalCentrePane = createCulturalCentrePane();
        GridPane varietyStagePane = createVarietyStagePane();

        dynamicFieldsContainer = new VBox(8);

        // Основной layout
        GridPane baseGrid = new GridPane();
        baseGrid.setHgap(10);
        baseGrid.setVgap(8);
        baseGrid.setPadding(new Insets(16));
        baseGrid.addRow(0, label("Название *"), nameField);
        baseGrid.addRow(1, label("Тип *"), typeCombo);
        baseGrid.addRow(2, label("Город"), cityField);
        baseGrid.addRow(3, label("Адрес"), addressField);
        baseGrid.addRow(4, label("Описание"), descriptionField);
        baseGrid.addRow(5, label("Детали"), dynamicFieldsContainer);

        getDialogPane().setContent(baseGrid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        // При смене типа подставляем нужную панель
        typeCombo.valueProperty().addListener((obs, old, newType) -> {
            updateDynamicFields(newType, theatrePane, cinemaPane, concertVenuePane, culturalCentrePane, varietyStagePane);
            getDialogPane().requestLayout();
            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            if (stage != null) {
                stage.sizeToScene();
            }
        });
        updateDynamicFields(typeCombo.getValue(), theatrePane, cinemaPane, concertVenuePane, culturalCentrePane, varietyStagePane);

        // Валидация: название и тип обязательны, плюс специфичные обязательные поля
        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        Runnable validate = () -> {
            boolean baseOk = !nameField.getText().trim().isEmpty();
            String type = typeCombo.getValue();
            boolean specificOk = true;
            if ("CINEMA".equals(type)) {
                specificOk = screenWidthField.getText().trim().isEmpty() == false &&
                        screenHeightField.getText().trim().isEmpty() == false;
            } else if ("THEATRE".equals(type)) {
                // можно добавить валидацию, если поля обязательны
                // specificOk = capacityField.getText().trim().isEmpty() == false;
            }
            okButton.setDisable(!baseOk || !specificOk);
        };
        nameField.textProperty().addListener((o, ov, nv) -> validate.run());
        typeCombo.valueProperty().addListener((o, ov, nv) -> validate.run());
        screenWidthField.textProperty().addListener((o, ov, nv) -> validate.run());
        screenHeightField.textProperty().addListener((o, ov, nv) -> validate.run());
        validate.run();

        setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> data = new HashMap<>();
            data.put("name", nameField.getText().trim());
            data.put("venueType", typeCombo.getValue());
            data.put("city", cityField.getText().trim());
            data.put("address", addressField.getText().trim());
            data.put("description", descriptionField.getText().trim());

            String type = typeCombo.getValue();
            switch (type) {
                case "THEATRE":
                    data.put("capacity", parseInt(capacityField.getText()));
                    data.put("stageWidthMm", parseInt(stageWidthField.getText()));
                    data.put("stageDepthMm", parseInt(stageDepthField.getText()));
                    break;
                case "CINEMA":
                    data.put("screenWidthMm", parseInt(screenWidthField.getText()));
                    data.put("screenHeightMm", parseInt(screenHeightField.getText()));
                    data.put("screenDiagonalMm", parseInt(screenDiagonalField.getText()));
                    data.put("screenAspectRatio", screenAspectRatioField.getText().trim().isEmpty() ? null : screenAspectRatioField.getText().trim());
                    break;
                case "CONCERT_VENUE":
                    data.put("stageType", stageTypeField.getText().trim().isEmpty() ? null : stageTypeField.getText().trim());
                    data.put("hasSoundSystem", hasSoundSystemCheck.isSelected());
                    break;
                case "CULTURAL_CENTRE":
                    data.put("communityRoomsCount", parseInt(communityRoomsCountField.getText()));
                    break;
                case "VARIETY_STAGE":
                    data.put("genreFocus", genreFocusField.getText().trim().isEmpty() ? null : genreFocusField.getText().trim());
                    break;
            }
            return data;
        });

        // Устанавливаем размер и возможность растягивания
        setOnShowing(ev -> {
            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            stage.setResizable(true);
            stage.setMinWidth(400);
            stage.setMinHeight(300);
        });
    }

    private GridPane createTheatrePane() {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        gp.addRow(0, label("Вместимость"), capacityField);
        gp.addRow(1, label("Ширина сцены (мм)"), stageWidthField);
        gp.addRow(2, label("Глубина сцены (мм)"), stageDepthField);
        return gp;
    }

    private GridPane createCinemaPane() {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        gp.addRow(0, label("Ширина экрана (мм) *"), screenWidthField);
        gp.addRow(1, label("Высота экрана (мм) *"), screenHeightField);
        gp.addRow(2, label("Диагональ (мм)"), screenDiagonalField);
        gp.addRow(3, label("Соотношение сторон"), screenAspectRatioField);
        return gp;
    }

    private GridPane createConcertVenuePane() {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        gp.addRow(0, label("Тип сцены"), stageTypeField);
        gp.addRow(1, new Label(), hasSoundSystemCheck);
        return gp;
    }

    private GridPane createCulturalCentrePane() {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        gp.addRow(0, label("Количество комнат"), communityRoomsCountField);
        return gp;
    }

    private GridPane createVarietyStagePane() {
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(6);
        gp.addRow(0, label("Жанровый фокус"), genreFocusField);
        return gp;
    }

    private void updateDynamicFields(String type, GridPane theatrePane, GridPane cinemaPane, GridPane concertVenuePane,
                                     GridPane culturalCentrePane, GridPane varietyStagePane) {
        dynamicFieldsContainer.getChildren().clear();
        switch (type) {
            case "THEATRE" -> dynamicFieldsContainer.getChildren().add(theatrePane);
            case "CINEMA" -> dynamicFieldsContainer.getChildren().add(cinemaPane);
            case "CONCERT_VENUE" -> dynamicFieldsContainer.getChildren().add(concertVenuePane);
            case "CULTURAL_CENTRE" -> dynamicFieldsContainer.getChildren().add(culturalCentrePane);
            case "VARIETY_STAGE" -> dynamicFieldsContainer.getChildren().add(varietyStagePane);
        }
    }

    private TextField createTextField(String val) {
        TextField tf = new TextField(val == null ? "" : val);
        tf.getStyleClass().add("text-field-lg");
        return tf;
    }

    private TextField createIntField(Integer val) {
        TextField tf = new TextField(val == null ? "" : val.toString());
        tf.getStyleClass().add("text-field-lg");
        return tf;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private Integer parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}