package fraymark.ui.javafx.viewmodel;

import fraymark.model.combatants.Combatant;
import javafx.beans.property.*;

/***
 * ViewModel for Combatants. <br>
 * - Model: accesses the Combatant as a model. <br>
 * - View: communicates information about the combatant through getters. <br>
 */
public class
CombatantViewModel {
    private final Combatant model;

    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty maxHp = new SimpleIntegerProperty();

    public CombatantViewModel(Combatant model) {
        this.model = model;
        name.bind(new SimpleStringProperty(model.getName()));
        maxHp.set(model.getStats().getMaxHP());
    }

    public StringProperty nameProperty() { return name; }
    public IntegerProperty hpProperty() { return model.getResources().hpProperty(); }
    public IntegerProperty maxHpProperty() { return maxHp; }
    public IntegerProperty mgProperty() { return model.getResources().mgProperty(); }
    public IntegerProperty trpProperty() { return model.getResources().trpProperty(); }

    public String getName() { return name.get(); }
}
