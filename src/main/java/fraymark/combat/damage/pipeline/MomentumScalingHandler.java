package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.physical.MomentumProfile;
import fraymark.model.actions.physical.Physical;

public class MomentumScalingHandler implements DamageHandler {
    @Override public boolean handle(DamageContext ctx) {
        if (!(ctx.action() instanceof Physical pa)) return false;
        MomentumProfile mp = pa.getMomentumProfile();
        if (mp == null) return false;

        int mg = ctx.source().getResources().getMg();
        double mul = mp.multiplier(mg);
        ctx.multiplyFinalDamage(mul);
        return false; // continue pipeline
    }
}

