package fraymark.ui.javafx.viewmodel;

import fraymark.model.combatants.Combatant;
import javafx.beans.property.*;

/***
 * ViewModel for Combatants. <br>
 * - Model: accesses the Combatant as a model. <br>
 * - View: communicates information about the combatant through getters. <br>
 */
public class CombatantViewModel {
    private final Combatant model;

    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty hp = new SimpleIntegerProperty();
    private final IntegerProperty maxHp = new SimpleIntegerProperty();
    private final IntegerProperty mg = new SimpleIntegerProperty();

    public CombatantViewModel(Combatant model) {
        this.model = model;
        sync();
    }

    public void sync() {
        name.set(model.getName());
        hp.set(model.getResources().getHp());
        maxHp.set(model.getStats().getMaxHP());
        mg.set(model.getResources().getMg());
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty hpProperty() { return hp; }
    public IntegerProperty maxHpProperty() { return maxHp; }
    public IntegerProperty mgProperty() { return mg; }
}
