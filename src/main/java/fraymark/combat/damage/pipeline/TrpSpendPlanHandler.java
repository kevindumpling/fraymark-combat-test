package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.weaves.TrpScalingProfile;
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
            case VARIABLE -> {
                TrpScalingProfile prof = w.getTrpScalingProfile();


                int maxByTrp = Math.max(0, available - base); // extra we can actually pay
                int minExtra = 0;
                int maxExtra = maxByTrp;

                if (prof != null) {
                    minExtra = Math.max(0, prof.minExtra());
                    if (prof.maxExtra() > 0 && prof.maxExtra() != Integer.MAX_VALUE) {
                        maxExtra = Math.min(maxByTrp, prof.maxExtra());
                    } else {
                        maxExtra = maxByTrp;
                    }
                }

                if (maxExtra <= 0) {
                    // Can't afford any extra; fall back to base only
                    yield base;
                }

                // Ensure minExtra does not exceed what we can pay
                int effMin = Math.min(minExtra, maxExtra);

                // Simple policy: spend as much as allowed
                int extra = maxExtra;

                // If you ever want a "greedy but not all-in" heuristic, tweak here.
                if (extra < effMin) extra = effMin;

                yield base + extra;
            }
            case ALL -> available;
        };

        ctx.setTrpToSpend(spend);
        return false;
    }
}
