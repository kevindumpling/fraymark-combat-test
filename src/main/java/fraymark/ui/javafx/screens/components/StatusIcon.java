package fraymark.ui.javafx.screens.components;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Placeholder for visual effect icons.
 * Can later observe Combatant's status list to auto-update itself.
 */
public class StatusIcon extends HBox {
    public StatusIcon() {
        getChildren().add(new ImageView());
    }
}