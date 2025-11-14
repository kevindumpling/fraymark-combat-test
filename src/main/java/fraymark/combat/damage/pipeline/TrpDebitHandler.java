package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.events.CombatEvent;
import fraymark.model.actions.weaves.Weave;

public class TrpDebitHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (!(ctx.action() instanceof Weave)) return false;

        // Only the primary DAMAGE context performs the debit.
        if (!ctx.isPrimaryHit()) return false;
        if (ctx.isTrpDebited()) return false;

        int spend = ctx.getTrpToSpend();
        if (spend <= 0) return false;

        var res = ctx.source().getResources();
        res.setTrp(Math.max(0, res.getTrp() - spend));
        ctx.markTrpDebited();

        if (ctx.bus() != null) {
            ctx.bus().publish(CombatEvent.logEvent(
                    ctx.source(), ctx.target(),
                    ctx.source().getName() + " spent " + spend + " TRP."
            ));
        }
        return false;
    }
}
