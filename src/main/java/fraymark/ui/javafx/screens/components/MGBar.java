package fraymark.ui.javafx.screens.components;

import fraymark.ui.javafx.viewmodel.CombatantViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/***
 * Display for the Momentum Gauge bar of a Combatant.
 */

public class MGBar extends ProgressBar {
    public MGBar(CombatantViewModel vm) {
        progressProperty().bind(
                Bindings.divide(vm.mgProperty().multiply(1.0), 100.0)
        );
    }
}