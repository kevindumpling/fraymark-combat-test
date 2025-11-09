package fraymark.ui.javafx.screens.components;

import fraymark.ui.javafx.viewmodel.CombatantViewModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/***
 * Display for the Momentum Gauge bar of a Combatant.
 */
public class MGBar extends ProgressBar {
    public MGBar(CombatantViewModel vm) {
        progressProperty().bind(
                vm.mgProperty().divide(100.0) // e.g., if MG max is 100
        );
    }
}