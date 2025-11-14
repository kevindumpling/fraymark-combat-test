// src/main/java/fraymark/model/effects/SimpleEffect.java
package fraymark.model.effects;

import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;

/***
 * A SimpleEffect is an effect with one temporary effect, such as BURN, DEF_DOWN, etc.
 */
public class SimpleEffect implements Effect {
    private final String name;
    private final EffectType type;
    private int magnitude;   // e.g., damage per tick or stat delta
    private int initialDuration;         // duration in turns

    public SimpleEffect(String name, EffectType type, int magnitude, int initialDuration) {
        this.name = name;
        this.type = type;
        this.magnitude = magnitude;
        this.initialDuration = initialDuration;
    }

    @Override
    public void onApply(Combatant target) {
        switch (type) {
            case DEF_DOWN -> target.getStats().setDef(target.getStats().getDef() - magnitude);
            case RES_DOWN -> target.getStats().setRes(target.getStats().getRes() - magnitude);
            default -> {}
        }
    }

    @Override
    public void onTurnStart(Combatant target) {

    }

    @Override
    public void onTurnEnd(Combatant target) {
        switch (type) {
            case BLEED, BURN -> target.getResources().enqueueRollingDamage(Math.max(0, magnitude));
            default -> {}
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void scaleMagnitude(double mul) {
        this.magnitude = (int)(this.magnitude*mul);
    }

    @Override
    public void onExpire(Combatant target, EventBus bus, ExpireReason reason) {
        switch (type) {
            case DEF_DOWN -> target.getStats().setDef(target.getStats().getDef() + magnitude);
            case RES_DOWN -> target.getStats().setRes(target.getStats().getRes() + magnitude);
            default -> {}
        }
    }

    @Override
    public double rollRateMultiplier() {
        return (type == EffectType.BLEED) ? 2.0 : 1.0; // tune as desired
    }

    @Override
    public int initialDuration(){
        return initialDuration;
    }
}
