package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.engine.EffectResolver;
import fraymark.model.effects.Effect;

/***
 * A CounterHandler is a DamageHandler that knows how to implement PHYSICAL counters.
 * TODO BROKEN
 */
public class CounterHandler implements DamageHandler {
    private final EffectResolver effects;

    public CounterHandler(EffectResolver effects) {
        this.effects = effects;
    }

    @Override
    public boolean handle(DamageContext ctx) {
        var target = ctx.target();

        boolean hasCounter = effects.hasEffectKey(target, "counter"); // whatever key you define
        if (!hasCounter) return false;

        double reflected = ctx.finalDamage() * 0.3;
        var srcRes = ctx.source().getResources();
        srcRes.setHp(Math.max(0, srcRes.getHp() - (int) reflected));

        // Optionally emit a combat event here

        return false;
    }
}
