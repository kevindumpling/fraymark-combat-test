// src/main/java/fraymark/model/effects/SimpleEffect.java
package fraymark.model.effects.impl;

import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;
import fraymark.model.effects.EffectType;
import fraymark.model.effects.ExpireReason;

/***
 * A SimpleEffect is an effect with one temporary effect, such as BURN, DEF_DOWN, etc.
 */
public class SimpleEffect implements Effect {
    private final String name;
    private final EffectType effectType;
    private int magnitude;   // e.g., damage per tick or stat delta
    private int initialDuration;         // duration in turns

    public SimpleEffect(String name, EffectType type, int magnitude, int initialDuration) {
        this.name = name;
        this.effectType = type;
        this.magnitude = magnitude;
        this.initialDuration = initialDuration;
    }

    @Override
    public void onApply(Combatant target) {
        switch (effectType) {
            case DEF_DOWN -> target.getStats().setDef(target.getStats().getDef() - magnitude);
            case RES_DOWN -> target.getStats().setRes(target.getStats().getRes() - magnitude);
            default -> {}
        }
    }

    @Override
    public void onTurnStart(Combatant target) {

    }

    @Override
    public int periodicDamage(Combatant source, Combatant target) {
        return switch (this.effectType) {
            case BLEED, BURN -> Math.max(0, magnitude);
            default -> 0;
        };
    }

    @Override
    public String periodicDamageMessage(Combatant source, Combatant target, int amount) {
        return switch (effectType) {
            case BURN -> target.getName() + " takes " + amount + " burn damage!";
            case BLEED -> target.getName() + " takes " + amount + " bleed damage!";
            default -> Effect.super.periodicDamageMessage(source, target, amount);
        };
    }

    @Override
    public void onTurnEnd(Combatant target) {
        // TODO
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
        switch (effectType) {
            case DEF_DOWN -> target.getStats().setDef(target.getStats().getDef() + magnitude);
            case RES_DOWN -> target.getStats().setRes(target.getStats().getRes() + magnitude);
            default -> {}
        }
    }

    @Override
    public double rollRateMultiplier() {
        return (effectType == EffectType.BLEED) ? 2.0 : 1.0; // tune as desired
    }

    @Override
    public int initialDuration(){
        return initialDuration;
    }
}
