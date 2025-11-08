package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;

/***
 * BarrierHandler is a type of DamageHandler that calculates damage absorption when resolving damage.
 */
public class BarrierHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        int barrier = ctx.target.getResources().getBarrier();
        if (barrier > 0) {
            double absorbed = Math.min(barrier, ctx.finalDamage);
            ctx.target.getResources().reduceBarrier(absorbed);
            ctx.finalDamage -= absorbed;
            if (ctx.finalDamage <= 0) {
                ctx.canceled = true;
                return true;
            }
        }
        return false;
    }
}