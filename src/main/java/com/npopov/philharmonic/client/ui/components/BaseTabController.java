package com.npopov.philharmonic.client.ui.components;

import com.npopov.philharmonic.client.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Predicate;

/**
 * Base controller for all data-table tabs.
 *
 * Subclasses implement:
 *  - buildColumns()      — add columns to tableView
 *  - loadData()          — return List<T> from API (runs on background thread)
 *  - buildFilter()       — return Predicate<T> based on current search text
 *  - showCreateDialog()  — open dialog for creating a new record
 *  - showEditDialog(T)   — open dialog to edit the selected record
 *  - deleteSelected(T)   — call API to delete the record
 */
public abstract class BaseTabController<T> {

    @FXML protected TableView<T>  tableView;
    @FXML protected TextField     searchField;
    @FXML protected Button        btnRefresh;
    @FXML protected Button        btnCreate;
    @FXML protected Button        btnEdit;
    @FXML protected Button        btnDelete;
    @FXML protected Label         statusLabel;
    @FXML protected HBox          filterBar;     // subclasses may add filter controls here
    @FXML protected ProgressIndicator loadingSpinner;

    protected final ObservableList<T> masterData    = FXCollections.observableArrayList();
    protected FilteredList<T>         filteredData;
    protected SortedList<T>           sortedData;

    @FXML
    public void initialize() {
        buildColumns();
        setupTableBinding();
        setupSearch();
        setupButtonStates();
        applyRolePermissions();
        refresh();
    }

    // ── Abstract API ─────────────────────────────────────────────────────────

    protected abstract void     buildColumns();
    protected abstract List<T>  loadData();
    protected abstract Predicate<T> buildFilter(String searchText);
    protected abstract void     showCreateDialog();
    protected abstract void     showEditDialog(T item);
    protected abstract void     deleteSelected(T item);

    // ── Wiring ───────────────────────────────────────────────────────────────

    private void setupTableBinding() {
        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData   = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
        tableView.setPlaceholder(new Label("Нет данных"));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(buildFilter(newVal == null ? "" : newVal.toLowerCase()))
        );
    }

    private void setupButtonStates() {
        // Edit and delete only active when something selected
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            btnEdit.setDisable(n == null);
            btnDelete.setDisable(n == null);
        });
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        // Double-click to edit
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                T selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) showEditDialog(selected);
            }
        });
    }

    private void applyRolePermissions() {
        SessionManager session = SessionManager.getInstance();
        btnCreate.setVisible(session.isAdmin());
        btnEdit.setVisible(session.isAdmin());
        btnDelete.setVisible(session.isSuperAdmin());
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    @FXML
    public void refresh() {
        setLoading(true);
        setStatus("Загрузка...");

        Task<List<T>> task = new Task<>() {
            @Override protected List<T> call() { return loadData(); }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            masterData.setAll(task.getValue());
            setLoading(false);
            setStatus("Записей: " + masterData.size());
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("Ошибка загрузки: " + task.getException().getMessage());
        }));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void handleCreate() { showCreateDialog(); }

    @FXML
    public void handleEdit() {
        T selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) showEditDialog(selected);
    }

    @FXML
    public void handleDelete() {
        T selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить выбранную запись?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    deleteSelected(selected);
                    refresh();
                } catch (Exception ex) {
                    showError("Ошибка удаления: " + ex.getMessage());
                }
            }
        });
    }

    // ── Utilities for subclasses ─────────────────────────────────────────────

    protected void setStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    protected void setLoading(boolean loading) {
        loadingSpinner.setVisible(loading);
        btnRefresh.setDisable(loading);
    }

    protected void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /** Helper: null-safe string for table cells */
    protected static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
