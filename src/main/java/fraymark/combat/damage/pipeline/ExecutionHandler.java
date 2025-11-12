// fraymark/combat/damage/pipeline/ExecutionHandler.java
package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.events.CombatEvent;
import fraymark.combat.events.CombatEventType;
import fraymark.model.actions.AttackRangeKind;
import fraymark.model.actions.physical.ExecutionProfile;
import fraymark.model.actions.physical.Physical;
import fraymark.model.combatants.Combatant;

public class ExecutionHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (ctx.canceled()) return true;
        if (!(ctx.action() instanceof Physical pa)) return false;
        if (!(pa.getRangeKind() == AttackRangeKind.MELEE || pa.getRangeKind() == AttackRangeKind.ALL)) return false;

        ExecutionProfile ex = pa.getExecutionProfile();
        if (ex == null || !ex.enabled()) return false;
        System.out.println("EXECUTION DEBUG " + ctx.source().getName());

        Combatant target = ctx.target();
        int hp = target.getResources().getHp();
        int max = target.getStats().getMaxHP();
        if (max <= 0) return false;

        double pct = (double) hp / (double) max;
        if (pct > ex.thresholdPct()) return false;

        if (ex.requiresNoArmor()) {
            System.out.println("EXECUTION DEBUG: target is armored");
            if (target.isArmored()) return false;

        }

        // Lethal: kill instantly, bypass rolling & damage
        target.getResources().forceKill();

        if (ctx.bus() != null) {
            String msg = ctx.source().getName() + " EXECUTED " + target.getName() + "!";
            ctx.bus().publish(new CombatEvent(
                    CombatEventType.LOG, ctx.source(), target, 0, msg
            ));
        }

        ctx.markExecuted(); // sets canceled=true
        return true;        // stop further handlers
    }
}
