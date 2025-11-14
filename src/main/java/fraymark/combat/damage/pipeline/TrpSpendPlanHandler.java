package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.weaves.TrpSpendMode;
import fraymark.model.actions.weaves.Weave;

public class TrpSpendPlanHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (!(ctx.action() instanceof Weave w)) return false;

        // Already planned (e.g., if engine prefilled)? Respect it.
        if (ctx.isTrpPlanned()) return false;

        int available = ctx.getTrpSnapshot();
        int base = Math.max(0, w.getTrpBaseCost());
        if (available < base) { // cannot cast
            ctx.cancel();
            return true;
        }

        int spend = base;
        TrpSpendMode mode = w.getTrpSpendMode();

        spend = switch (mode) {
            case FLAT -> // spend exactly base
                    base;
            case VARIABLE ->
                // policy: spend up to +20 bonus TRP if available (tune as you like)
                    Math.min(available, base + 20);
            case ALL -> available;
        };

        ctx.setTrpToSpend(spend);
        return false;
    }
}
