package fraymark.ui.javafx.viewmodel;

import fraymark.combat.engine.BattleState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import fraymark.model.combatants.Combatant;

/***
 * ViewModel for Battles, accessing their individual states. <br>
 * - Model: accesses the current BattleState as a model. <br>
 * - View: communicates information about the current BattleState through getters. <br>
 */
public class BattleViewModel {
    private final BattleState state;
    private final ObservableList<CombatantViewModel> partyVM = FXCollections.observableArrayList();
    private final ObservableList<CombatantViewModel> enemyVM = FXCollections.observableArrayList();

    public BattleViewModel(BattleState state) {
        this.state = state;
        state.getParty().forEach(c -> partyVM.add(new CombatantViewModel(c)));
        state.getEnemies().forEach(c -> enemyVM.add(new CombatantViewModel(c)));
    }

    public ObservableList<CombatantViewModel> getPartyVM() { return partyVM; }
    public ObservableList<CombatantViewModel> getEnemyVM() { return enemyVM; }
}