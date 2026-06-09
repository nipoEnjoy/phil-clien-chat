package com.npopov.philharmonic.client.ui.tabs;

import com.npopov.philharmonic.client.api.ArtistApiClient;
import com.npopov.philharmonic.client.api.GenreApiClient;
import com.npopov.philharmonic.client.api.ImpresarioApiClient;
import com.npopov.philharmonic.client.model.ArtistModel;
import com.npopov.philharmonic.client.model.GenreModel;
import com.npopov.philharmonic.client.model.ImpresarioModel;
import com.npopov.philharmonic.client.ui.components.BaseTabController;
import com.npopov.philharmonic.client.ui.util.DialogStyler;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArtistsTabController extends BaseTabController<ArtistModel> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private ComboBox<String> genreFilter;
    private ComboBox<ImpresarioModel> impresarioFilter;
    private CheckBox multipleGenresCheck;

    private List<GenreModel> allGenres = new ArrayList<>();

    @Override
    @FXML
    public void initialize() {
        // Создаём фильтры
        genreFilter = new ComboBox<>();
        genreFilter.setPromptText("Жанр");
        impresarioFilter = new ComboBox<>();
        impresarioFilter.setPromptText("Импресарио");
        multipleGenresCheck = new CheckBox("Несколько жанров");

        Button applyBtn = new Button("🔍 Найти");
        applyBtn.setOnAction(e -> refresh());
        Button resetBtn = new Button("✖ Сброс");
        resetBtn.setOnAction(e -> {
            genreFilter.getSelectionModel().clearSelection();
            impresarioFilter.getSelectionModel().clearSelection();
            multipleGenresCheck.setSelected(false);
            refresh();
        });

        filterBar.getChildren().addAll(genreFilter, impresarioFilter, multipleGenresCheck, applyBtn, resetBtn);

        // Загружаем данные для выпадающих списков
        loadGenres();
        loadImpresarios();

        super.initialize();
    }

    private void loadGenres() {
        Task<List<GenreModel>> task = new Task<>() {
            @Override protected List<GenreModel> call() { return GenreApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            allGenres = task.getValue();

            List<String> genreNames = allGenres.stream()
                    .map(GenreModel::getName)
                    .collect(Collectors.toList());
            genreFilter.setItems(FXCollections.observableArrayList(genreNames));
        });
        new Thread(task).start();
    }

    private void loadImpresarios() {
        Task<List<ImpresarioModel>> task = new Task<>() {
            @Override protected List<ImpresarioModel> call() { return ImpresarioApiClient.getInstance().findAll(); }
        };
        task.setOnSucceeded(e -> {
            impresarioFilter.setItems(FXCollections.observableArrayList(task.getValue()));
            impresarioFilter.setConverter(new StringConverter<>() {
                @Override public String toString(ImpresarioModel i) { return i == null ? "" : i.getFullName(); }
                @Override public ImpresarioModel fromString(String s) { return null; }
            });
        });
        new Thread(task).start();
    }

    @Override
    protected List<ArtistModel> loadData() {
        String genre = genreFilter.getValue();
        ImpresarioModel impresario = impresarioFilter.getValue();
        boolean multiple = multipleGenresCheck.isSelected();

        if (genre != null && !genre.isBlank()) {
            return ArtistApiClient.getInstance().findByGenre(genre);
        } else if (impresario != null) {
            return ArtistApiClient.getInstance().findByImpresario(impresario.getId());
        } else if (multiple) {
            return ArtistApiClient.getInstance().findWithMultipleGenres();
        } else {
            return ArtistApiClient.getInstance().findAll();
        }
    }

    @Override
    protected void buildColumns() {
        TableColumn<ArtistModel, Long>   colId        = col("ID",           60,  "id");
        TableColumn<ArtistModel, String> colFirst     = col("Имя",          120, "firstName");
        TableColumn<ArtistModel, String> colLast      = col("Фамилия",      120, "lastName");
        TableColumn<ArtistModel, String> colStage     = col("Псевдоним",    140, "stageName");
        TableColumn<ArtistModel, String> colContact   = col("Контакт",      160, "contactInfo");

        TableColumn<ArtistModel, String> colGenres = new TableColumn<>("Жанры");
        colGenres.setPrefWidth(200);
        colGenres.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getGenresString()));

        TableColumn<ArtistModel, String> colCreated = new TableColumn<>("Создан");
        colCreated.setPrefWidth(130);
        colCreated.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        cd.getValue().getCreatedAt() != null
                                ? cd.getValue().getCreatedAt().format(FMT) : ""));

        tableView.getColumns().addAll(colId, colFirst, colLast, colStage, colContact, colGenres, colCreated);
        tableView.getSortOrder().add(colId);
    }

    @Override
    protected Predicate<ArtistModel> buildFilter(String q) {
        if (q == null || q.isBlank()) return p -> true;
        return a -> str(a.getFirstName()).toLowerCase().contains(q)
                || str(a.getLastName()).toLowerCase().contains(q)
                || str(a.getStageName()).toLowerCase().contains(q)
                || str(a.getContactInfo()).toLowerCase().contains(q)
                || str(a.getGenresString()).toLowerCase().contains(q);
    }

    @Override
    protected void showCreateDialog() {
        ArtistDialog dialog = new ArtistDialog(null, allGenres);
        dialog.showAndWait().ifPresent(result -> {
            try {
                ArtistModel created = ArtistApiClient.getInstance().create(result.artistData);

                syncGenres(created.getId(), Collections.emptyList(), result.selectedGenres);

                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    @Override
    protected void showEditDialog(ArtistModel item) {
        ArtistDialog dialog = new ArtistDialog(item, allGenres);
        dialog.showAndWait().ifPresent(result -> {
            try {
                ArtistApiClient.getInstance().update(item.getId(), result.artistData);

                List<Long> oldGenreIds = item.getGenres() != null
                        ? item.getGenres().stream().map(GenreModel::getId).collect(Collectors.toList())
                        : Collections.emptyList();
                syncGenres(item.getId(), oldGenreIds, result.selectedGenres);

                refresh();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }

    private void syncGenres(Long artistId, List<Long> oldGenreIds, List<Long> newGenreIds) {
        // Удаляем жанры, которых нет в новом списке
        for (Long oldId : oldGenreIds) {
            if (!newGenreIds.contains(oldId)) {
                ArtistApiClient.getInstance().removeGenre(artistId, oldId);
            }
        }
        // Добавляем новые жанры
        for (Long newId : newGenreIds) {
            if (!oldGenreIds.contains(newId)) {
                ArtistApiClient.getInstance().addGenre(artistId, newId);
            }
        }
    }

    @Override
    protected void deleteSelected(ArtistModel item) {
        ArtistApiClient.getInstance().delete(item.getId());
    }

    // ── Column helper ────────────────────────────────────────────────────────
    private <V> TableColumn<ArtistModel, V> col(String title, double width, String prop) {
        TableColumn<ArtistModel, V> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    // ── Inner dialog ─────────────────────────────────────────────────────────
    private static class ArtistDialog extends Dialog<ArtistDialogResult> {

        ArtistDialog(ArtistModel existing, List<GenreModel> allGenres) {
            setTitle(existing == null ? "Новый артист" : "Редактировать артиста");
            setHeaderText(null);

            TextField firstName   = field(existing != null ? existing.getFirstName()   : "");
            TextField lastName    = field(existing != null ? existing.getLastName()    : "");
            TextField stageName   = field(existing != null ? existing.getStageName()   : "");
            TextField contactInfo = field(existing != null ? existing.getContactInfo() : "");

            final Set<Long> selectedGenreIds = new HashSet<>();
            if (existing != null && existing.getGenres() != null) {
                existing.getGenres().forEach(g -> selectedGenreIds.add(g.getId()));
            }

            ListView<GenreModel> genreListView = new ListView<>();
            genreListView.setItems(FXCollections.observableArrayList(allGenres));
            genreListView.setPrefHeight(150);
            genreListView.setPrefWidth(220);

            genreListView.setCellFactory(lv -> new ListCell<>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    // При клике на CheckBox переключаем выбор
                    checkBox.setOnAction(e -> {
                        GenreModel genre = getItem();
                        if (genre != null) {
                            if (checkBox.isSelected()) {
                                selectedGenreIds.add(genre.getId());
                            } else {
                                selectedGenreIds.remove(genre.getId());
                            }
                        }
                    });

                    // Чтобы клик по строке тоже переключал CheckBox
                    setOnMouseClicked(e -> {
                        GenreModel genre = getItem();
                        if (genre != null && !isEmpty()) {
                            checkBox.setSelected(!checkBox.isSelected());
                            checkBox.getOnAction().handle(null);
                        }
                    });
                }

                @Override
                protected void updateItem(GenreModel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        checkBox.setText(item.getName());
                        checkBox.setSelected(selectedGenreIds.contains(item.getId()));
                        setGraphic(checkBox);
                    }
                }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(8);
            grid.addRow(0, label("Имя *"),      firstName);
            grid.addRow(1, label("Фамилия"),    lastName);
            grid.addRow(2, label("Псевдоним"),  stageName);
            grid.addRow(3, label("Контакт"),    contactInfo);
            grid.addRow(4, label("Жанры"),      genreListView);
            grid.setPadding(new Insets(16));
            firstName.setPrefWidth(220);

            getDialogPane().setContent(grid);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());

            DialogStyler.applyStyles(this);

            Button okBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okBtn.setDisable(true);
            firstName.textProperty().addListener((o, ov, nv) ->
                    okBtn.setDisable(nv == null || nv.isBlank()));
            if (existing != null && existing.getFirstName() != null && !existing.getFirstName().isBlank())
                okBtn.setDisable(false);

            setResultConverter(btn -> {
                if (btn != ButtonType.OK) return null;

                Map<String, Object> artistData = new HashMap<>();
                artistData.put("firstName", firstName.getText().trim());
                artistData.put("lastName", lastName.getText().trim());
                artistData.put("stageName", stageName.getText().trim());
                artistData.put("contactInfo", contactInfo.getText().trim());

                // Используем уже существующий selectedGenreIds
                List<Long> selectedGenres = new ArrayList<>(selectedGenreIds);

                return new ArtistDialogResult(artistData, selectedGenres);
            });
        }

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

    private static class ArtistDialogResult {
        final Map<String, Object> artistData;
        final List<Long> selectedGenres;

        ArtistDialogResult(Map<String, Object> artistData, List<Long> selectedGenres) {
            this.artistData = artistData;
            this.selectedGenres = selectedGenres;
        }
    }
}