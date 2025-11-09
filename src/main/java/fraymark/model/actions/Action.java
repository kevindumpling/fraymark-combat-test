package fraymark.model.actions;
import fraymark.model.combatants.Combatant;

/***
 * An Action is something that can be performed in combat.
 */
public interface Action {

    String getName();

    ActionType getType();

    int getTrpCost();

    ActionResult execute(ActionContext context);

    boolean canUse(Combatant user);

}