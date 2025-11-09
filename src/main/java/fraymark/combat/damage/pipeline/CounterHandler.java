package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.effects.Effect;

/***
 * A CounterHandler is a DamageHandler that knows how to implement PHYSICAL counters.
 */
public class CounterHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        for (Effect e : ctx.target().getStatusEffects()) {
            if (e.getName().equalsIgnoreCase("Counter")) {
                double reflected = ctx.finalDamage() * 0.3;
                ctx.source().getResources().setHp(
                        Math.max(0, (int)(ctx.source().getResources().getHp() - reflected))
                );
            }
        }
        return false;
    }
}