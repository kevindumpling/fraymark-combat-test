package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;
import java.util.Iterator;
import java.util.List;

/***
 * Beginning-to-end resolution of effects.
 */
public class EffectResolver {

    public void onTurnStart(List<Combatant> all) {
        for (Combatant c : all) {
            for (Effect e : c.getStatusEffects()) e.onTurnStart(c);
        }
    }

    public void onTurnEnd(List<Combatant> all) {
        for (Combatant c : all) {
            Iterator<Effect> it = c.getStatusEffects().iterator();
            while (it.hasNext()) {
                Effect e = it.next();
                e.onTurnEnd(c);
                if (e.isExpired()) it.remove();
            }
        }
    }
}