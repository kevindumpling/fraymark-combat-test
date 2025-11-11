// src/main/java/fraymark/model/effects/SimpleEffect.java
package fraymark.model.effects;

import fraymark.model.combatants.Combatant;

/***
 * A SimpleEffect is an effect with one temporary effect, such as BURN, DEF_DOWN, etc.
 */
public class SimpleEffect implements Effect {
    private final String name;
    private final EffectType type;
    private final int magnitude;   // e.g., damage per tick or stat delta
    private int remaining;         // duration in turns

    public SimpleEffect(String name, EffectType type, int magnitude, int duration) {
        this.name = name;
        this.type = type;
        this.magnitude = magnitude;
        this.remaining = duration;
    }

    @Override
    public void onApply(Combatant target) {
        // one-time application (e.g., apply debuff immediately)
        switch (type) {
            case DEF_DOWN -> target.getStats().setDef(target.getStats().getDef()-magnitude); // you may need a temp system
            case RES_DOWN -> target.getStats().setRes(target.getStats().getRes()-magnitude);
            default -> { /* no-op here */ }
        }
    }

    @Override
    public void onTurnStart(Combatant target) {
        // Start-of-turn effects (e.g., STUN could be handled here if supported)
    }

    @Override
    public void onTurnEnd(Combatant target) {
        // Periodic ticks
        switch (type) {
            case BLEED, BURN -> {
                int hp = target.getResources().getHp();
                int newHp = Math.max(0, hp - magnitude);
                target.getResources().setHp(newHp);
            }
            default -> { /* no periodic tick */ }
        }
        remaining = Math.max(0, remaining - 1);

        // On final turn, revert temp stat mods
        if (remaining == 0) {
            switch (type) {
                case DEF_DOWN -> target.getStats().setDef(target.getStats().getDef()+magnitude);
                case RES_DOWN -> target.getStats().setRes(target.getStats().getRes()-magnitude);
                default -> { }
            }
        }
    }

    @Override
    public boolean isExpired() {
        return remaining <= 0;
    }

    @Override
    public String getName() {
        return name;
    }
}
