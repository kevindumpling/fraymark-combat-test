package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.weaves.Weave;

public class ResHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (ctx.canceled()) return true;
        // Only for weaves; skip physicals
        if (!(ctx.action() instanceof Weave)) return false;

        var target = ctx.target();
        int res = target.getStats().getRes(); // add getter if missing
        int flat = ctx.getResBypassFlat();
        double pct = ctx.getResBypassPct();

        double effectiveRes = Math.max(0, (res - flat) * (1.0 - pct));

        double in = ctx.finalDamage();
        double out = in * (100.0 / (100.0 + effectiveRes)); // same curve as DEF
        ctx.setFinalDamage(out);
        return false;
    }
}

