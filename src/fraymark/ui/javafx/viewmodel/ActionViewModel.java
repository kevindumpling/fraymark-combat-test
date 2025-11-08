package fraymark.ui.javafx.viewmodel;

import fraymark.model.actions.Action;
import javafx.beans.property.*;

/***
 * ViewModel for Actions. <br>
 * - Model: accesses the Action as a model. <br>
 * - View: communicates information about the Action through getters. <br>
 */
public class ActionViewModel {
    private final Action action;
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty trpCost = new SimpleIntegerProperty();

    public ActionViewModel(Action action) {
        this.action = action;
        name.set(action.getName());
        trpCost.set(action.getTrpCost());
    }

    public StringProperty nameProperty() { return name; }
    public IntegerProperty trpCostProperty() { return trpCost; }
    public Action getAction() { return action; }
}