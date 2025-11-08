package fraymark.model.actions;
import fraymark.model.combatants.Combatant;

/***
 * An InterruptableAction is an Action which permits an Interruption to change its ActionResult before its resolution.
 */
public interface InterruptibleAction extends Action {
    boolean canBeInterrupted();
    void onInterrupted(Combatant source, Combatant interrupter);
}
