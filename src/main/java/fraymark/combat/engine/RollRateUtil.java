package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;

public final class RollRateUtil {
    private RollRateUtil(){}

    public static double computeMultiplier(Combatant c) {
        double m = 1.0;
        for (Effect e : c.getStatusEffects()) {
            m *= e.rollRateMultiplier();  // default 1.0 for most; Bleed can return 2.0, etc.
        }
        return Math.max(0.0, m);
    }
}
