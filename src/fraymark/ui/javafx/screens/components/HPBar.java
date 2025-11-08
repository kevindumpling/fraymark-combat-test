package fraymark.ui.javafx.screens.components;

import fraymark.ui.javafx.viewmodel.CombatantViewModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/***
 * A display for a Combatant's HP bar.
 */
public class HPBar extends HBox {
    public HPBar(CombatantViewModel vm) {
        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(150);
        bar.progressProperty().bind(vm.hpProperty().divide(vm.maxHpProperty().multiply(1.0)));
        Text label = new Text();
        label.textProperty().bind(vm.hpProperty().asString("HP: %d"));
        getChildren().addAll(label, bar);
    }
}