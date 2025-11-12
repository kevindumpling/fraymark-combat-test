package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.AttackRangeKind;
import fraymark.model.actions.physical.CloseRangeProfile;
import fraymark.model.actions.physical.Physical;

public class CloseRangeBonusHandler implements DamageHandler {
    @Override public boolean handle(DamageContext ctx) {
        if (!(ctx.action() instanceof Physical pa)) return false;
        if (ctx.getRangeKind() != AttackRangeKind.MELEE &&
                ctx.getRangeKind() != AttackRangeKind.ALL) return false;
        if (!ctx.getTargetIsClose()) return false;

        CloseRangeProfile cr = pa.getCloseRangeProfile();
        if (cr == null) return false;

        ctx.multiplyFinalDamage(cr.damageMul());
        ctx.addDefenseBypassPct(cr.defBypassPct());
        ctx.multiplyMgGain(cr.mgGainMul());
        return false;
    }
}
