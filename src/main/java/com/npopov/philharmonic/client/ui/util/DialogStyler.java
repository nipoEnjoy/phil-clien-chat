package com.npopov.philharmonic.client.ui.util;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public final class DialogStyler {

    private static final String CSS_PATH = "/css/app.css";

    private DialogStyler() {}

    /**
     * Применяет глобальные стили к DialogPane.
     * Необходимо вызывать после создания любого Dialog.
     */
    public static <T> void applyStyles(Dialog<T> dialog) {
        DialogPane pane = dialog.getDialogPane();

        // Подключаем CSS
        String css = DialogStyler.class.getResource(CSS_PATH).toExternalForm();
        if (!pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }

        // Применяем стиль к самому Dialog (для фона)
        if (!dialog.getDialogPane().getStyleClass().contains("custom-dialog")) {
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        }
    }
}