package fraymark.model.effects;

import fraymark.model.combatants.Combatant;

/***
 * An Effect is some secondary effect to an Action.
 */
public interface Effect {

    void onApply(Combatant target);

    void onTurnStart(Combatant target);

    void onTurnEnd(Combatant target);

    boolean isExpired();

    String getName();
}
