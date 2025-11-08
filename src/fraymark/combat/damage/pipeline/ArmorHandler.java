package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;

/***
 * An ArmorHandler is a DamageHandler that can reduce the incoming damage based on armor.
 */
public class ArmorHandler implements DamageHandler {
    @Override
    public boolean handle(DamageContext ctx) {
        if (ctx.target.isArmored()) {
            ctx.finalDamage *= 0.9; // slight reduction
        }
        return false;
    }
}