package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.physical.Physical;
import fraymark.model.combatants.Combatant;

public class DefenseHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (ctx.canceled()) return true;
        // Only for physicals; skip weaves
        if (!(ctx.action() instanceof Physical)) return false;

        Combatant target = ctx.target();
        int def = target.getStats().getDef();

        // Apply bypass coming from earlier handlers (e.g., CloseRange)
        int flat = ctx.getDefBypassFlat();
        double pct = ctx.getDefBypassPct();

        double effectiveDef = Math.max(0, (def - flat) * (1.0 - pct));

        double in = ctx.finalDamage();
        double out = in * (100.0 / (100.0 + effectiveDef)); // smooth reduction
        ctx.setFinalDamage(out);

        return false; // continue
    }
}
