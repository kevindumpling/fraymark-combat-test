package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.engine.RollRateUtil;
import fraymark.combat.events.CombatEvent;
import fraymark.combat.events.CombatEventType;
import fraymark.model.combatants.Combatant;

/***
 * Final stage of the damage pipeline.
 * Applies computed damage to the target and emits a CombatEvent for UI updates.
 */
public class ApplyDamageHandler implements DamageHandler {

    @Override
    public boolean handle(DamageContext ctx) {
        if (ctx.canceled()) {
            return true; // damage was canceled upstream
        }

        Combatant target = ctx.target();
        Combatant source = ctx.source();
        double amount = ctx.finalDamage();

        // get roll rate multiplier, default 1.0
        double mult = RollRateUtil.computeMultiplier(target);
        target.getResources().setRollRateMultiplier(mult);

        target.getResources().enqueueRollingDamage((int)amount);  // apply the damage as rolling damage

        if (ctx.bus() != null) {
            String msg = String.format(
                    "(%s took %d damage!)",
                    target.getName(),
                    (int) amount
            );

            ctx.bus().publish(new CombatEvent(
                    CombatEventType.DAMAGE,
                    source,
                    target,
                    amount,
                    msg
            ));
        }

        // Always end the chain — this is the final handler
        return true;
    }
}
