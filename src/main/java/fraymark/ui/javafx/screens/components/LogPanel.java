package fraymark.ui.javafx.screens.components;

import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Logs combat output.
 */
public class LogPanel extends VBox {
    private final TextArea log = new TextArea();

    public LogPanel() {
        log.setEditable(false);
        log.setPrefHeight(150);
        getChildren().add(log);
    }

    public void append(String msg) {
        log.appendText(msg + "\n");
    }
}