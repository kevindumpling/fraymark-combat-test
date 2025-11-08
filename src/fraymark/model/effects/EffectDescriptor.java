package fraymark.model.effects;

import fraymark.model.combatants.Combatant;

/***
 * An EffectDescriptor adds a source -> target description to an Effect.
 */
public interface EffectDescriptor {
    Effect instantiate(Combatant source, Combatant target);
}