package fraymark.ui.javafx.screens.components;

import fraymark.ui.javafx.viewmodel.CombatantViewModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/***
 * Display for the Momentum Gauge bar of a Combatant.
 */
public class MGBar extends HBox {
    public MGBar(CombatantViewModel vm) {
        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(150);
        bar.progressProperty().bind(vm.mgProperty().divide(100.0));
        Text label = new Text("MG");
        getChildren().addAll(label, bar);
    }
}