package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;

import static java.lang.Math.max;

/***
 * BarrierHandler is a type of DamageHandler that calculates damage absorption when resolving damage.
 */
public class BarrierHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        double barrier = ctx.target().getResources().getBarrier();  // in pct. from 0.0-1.0.
        double ignore = max(0, Math.min(1, ctx.getBarrierIgnorePct()));
        double effectiveBarrier = barrier - ignore;

        if (effectiveBarrier > 0) {
            double absorbed = max(effectiveBarrier * ctx.finalDamage(), 0);
            ctx.setFinalDamage((int)(ctx.finalDamage()*absorbed));
            if (ctx.finalDamage() <= 0) {
                ctx.cancel();
                return true;
            }
        }
        return false;
    }
}