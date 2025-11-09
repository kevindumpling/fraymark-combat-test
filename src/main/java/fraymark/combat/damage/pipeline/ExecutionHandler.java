package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;

/***
 * An ExecutionHandler is a DamageHandler that can instantly kill targets when they are low enough health
 * and struck by an Execution-type attack.
 */
public class ExecutionHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        int hp = ctx.target().getResources().getHp();
        int maxHP = ctx.target().getStats().getMaxHP();
        if (hp < maxHP * 0.25 && ctx.basePower() >= 80) {
            ctx.setFinalDamage(hp); // guaranteed lethal
        }
        return false;
    }
}