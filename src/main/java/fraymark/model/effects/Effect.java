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

    void scaleMagnitude(double mul);

    default double rollRateMultiplier() { return 1.0; }  // override in Bleed/Poison/etc. if desired
}
