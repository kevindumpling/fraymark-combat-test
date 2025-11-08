package fraymark.model.actions;

import fraymark.model.combatants.Combatant;
import java.util.List;

/***
 * ActionContext is a record that contains any additional information that needs to be given to an Action for it to work,
 * mainly its user and targets.
 */
public record ActionContext(Combatant user, List<Combatant> targets) {
}