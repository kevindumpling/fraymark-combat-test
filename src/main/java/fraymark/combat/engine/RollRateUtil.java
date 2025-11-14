package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;

public final class RollRateUtil {
    private RollRateUtil(){}

    public static double computeMultiplier(Combatant c) {
        // EffectResolver already maintains this
        return Math.max(0.0, c.getResources().getRollRateMultiplier());
    }
}
