package fraymark.model.effects;

import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;

/***
 * An Effect is some secondary effect to an Action.
 */
public interface Effect {

    /**
     * Any damage per turn behavior goes here.
     * @param source
     * @param target
     * @return amount of damage
     */
    default int periodicDamage(Combatant source, Combatant target) {
        return 0;
    }

    /** Optional: custom message for periodic damage. */
    default String periodicDamageMessage(Combatant source, Combatant target, int amount) {
        return target.getName() + " takes " + amount + " damage from " + getName() + "!";
    }

    void onApply(Combatant target);

    /***
     * Any non-damage per turn things go here.
     * @param target
     */
    void onTurnStart(Combatant target);

    /***
     * Any non-damage per turn things go here.
     * @param target
     */
    void onTurnEnd(Combatant target);

    String getName();

    void scaleMagnitude(double mul);

    // called exactly once when the effect is removed (duration ends, dispel, overwrite)
    default void onExpire(Combatant target, EventBus bus, ExpireReason reason) {}

    // a stable key to handle uniqueness/stacking (e.g., "BARRIER:0.30")
    default String key() { return getClass().getName(); }

    // how this effect stacks with another of the same key
    default StackingRule stacking() { return StackingRule.REFRESH_DURATION; }

    // default duration in turns (0/negative means “indefinite” unless you manage it externally)
    default int initialDuration() { return 0; }

    default double rollRateMultiplier() { return 1.0; } // 1.0 = no change
}
