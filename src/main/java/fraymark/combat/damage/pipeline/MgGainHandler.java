package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.events.CombatEvent;
import fraymark.model.actions.physical.Physical;

public class MgGainHandler implements DamageHandler {
    @Override public boolean handle(DamageContext ctx) {
        if (!(ctx.action() instanceof Physical pa)) return false;

        int base = ctx.getBaseMgGain();
        if (base <= 0) return false;

        int gain = (int)Math.round(base * ctx.getMgGainMultiplier());
        if (gain <= 0) return false;

        var res = ctx.source().getResources();
        res.setMg(res.getMg() + gain);

        if (ctx.bus() != null) {
            ctx.bus().publish(CombatEvent.logEvent(
                    ctx.source(), ctx.target(),
                    ctx.source().getName() + " gains +" + gain + " MG."));
        }
        return false;
    }
}
