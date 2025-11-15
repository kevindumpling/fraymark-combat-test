package fraymark.model.fields;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.engine.BattleState;
import fraymark.combat.events.CombatEvent;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.ExpireReason;

import java.util.Collections;
import java.util.List;

public interface Field {
    String id();
    String getName();
    String getDescription();

    default int modifyTrpGain(DamageContext ctx, int currentGain) {
        return currentGain; // default: no change
    }

    /** How many full rounds this field lasts. 0 or negative = infinite. */
    default int initialDuration() { return 0; }

    /** Called once when the field actually becomes active (on first turn start). */
    default void onApply(BattleState state) {}

    /** Called when the field expires or is removed. */
    default void onExpire(BattleState state, ExpireReason reason) {}

    /** Per-actor start-of-turn hook. */
    default void onTurnStart(BattleState state, Combatant actor) {}

    /** Per-actor start-of-turn hook. */
    default List<CombatEvent> onTurnEnd(BattleState state) { System.out.println("FIELD: DEFAULT ON TURN END CALLED"); return List.of();   };

    /** Per-round hook to drive non-damage behaviour; usually used by AoE stuff. */
    default void onRoundTick(BattleState state) {}

    /** Damage-time hook: modify damage/TRP based on environment. */
    default void onDamage(DamageContext ctx) {}

    /**
     * Optional periodic AoE damage.
     * Called once per round per target by the FieldManager.
     */
    default int periodicDamage(BattleState state, Combatant target) { return 0; }

    /** Custom log text for that periodic damage. */
    default String periodicDamageMessage(BattleState state, Combatant target, int amount) {
        return target.getName() + " takes " + amount + " damage from " + getName() + "!";
    }
}
