package fraymark.model.effects.factory;

import fraymark.model.effects.*;
import fraymark.model.effects.impl.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Registry-based factory that maps an EffectType -> EffectDescriptor generator. <br>
 * Later, it should be loaded from JSON in fraymark/data/effects.json.
 */
public class EffectFactory {
    private final Map<EffectType, EffectDescriptor> registry = new HashMap<>();

    public EffectFactory() {
        // Defaults are registered here.
        // Example: registry.put(EffectType.BURN, new BurnDescriptor(15, 3));
    }

    public void register(EffectType type, EffectDescriptor descriptor) {
        registry.put(type, descriptor);
    }

    public EffectDescriptor get(EffectType type) {
        return registry.get(type);
    }

    public Effect create(EffectType type, fraymark.model.combatants.Combatant src, fraymark.model.combatants.Combatant tgt) {
        EffectDescriptor desc = registry.get(type);
        return (desc != null) ? desc.instantiate(src, tgt) : null;
    }
}