package fraymark.ui.javafx.screens.components;

import fraymark.ui.javafx.viewmodel.CombatantViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/***
 * A display for a Combatant's HP bar.
 */
public class HPBar extends ProgressBar {
    public HPBar(CombatantViewModel vm) {
        DoubleBinding ratio = Bindings.createDoubleBinding(
                () -> {
                    double hp = vm.hpProperty().get();
                    double max = Math.max(1, vm.maxHpProperty().get()); // avoid /0
                    return hp / max;
                },
                vm.hpProperty(),
                vm.maxHpProperty()
        );

        progressProperty().bind(ratio);
    }
}