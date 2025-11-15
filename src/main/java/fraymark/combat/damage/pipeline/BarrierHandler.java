package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;

import static java.lang.Math.max;

/***
 * BarrierHandler is a type of DamageHandler that calculates damage absorption when resolving damage.
 */
public class BarrierHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        double barrier = ctx.target().getResources().getBarrier();     // 0 to 1
        double ignore  = Math.max(0, Math.min(1, ctx.getBarrierIgnorePct()));

        double effectiveBarrier = Math.max(0.0, Math.min(1.0, barrier - ignore));
        if (effectiveBarrier <= 0) return false;

        double in       = ctx.finalDamage();
        double absorbed = in * effectiveBarrier;
        double out      = in - absorbed;                    // ← keep the remainder

        System.out.println("BARRIERHANDLER: FINAL DAMAGE" + out + " AND IGNORE PCT " + barrier);
        ctx.setFinalDamage(out);

        if (out <= 0) { ctx.cancel(); return true; }
        return false;
    }
}
