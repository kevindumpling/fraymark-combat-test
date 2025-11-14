package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.weaves.TrpScalingProfile;
import fraymark.model.actions.weaves.TrpSpendMode;
import fraymark.model.actions.weaves.Weave;

public class TrpScalingHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (!(ctx.action() instanceof Weave w)) return false;

        int spend = ctx.getTrpToSpend();
        if (spend <= 0) return false;

        TrpScalingProfile prof = w.getTrpScalingProfile();
        if (prof == null) return false; // no scaling configured

        int base = Math.max(0, w.getTrpBaseCost());
        int extra = Math.max(0, spend - base);

        double mul = 1.0 + prof.perPointMul() * extra;
        if (prof.capMul() > 0.0) mul = Math.min(mul, prof.capMul());

        ctx.multiplyFinalDamage(mul);
        return false;
    }
}
