package fraymark.model.effects.factory;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;
import java.util.*;
import java.util.function.Function;

public class EffectFactory {
    private final Map<String, Function<Map<String, Object>, EffectDescriptor>> registry = new HashMap<>();

    public EffectFactory() {

    }

    public void register(String type, Function<Map<String,Object>, EffectDescriptor> constructor) {
        registry.put(type.toUpperCase(Locale.ROOT), constructor);
    }

    public void loadFromData(List<Map<String,Object>> effectData) {
        for (Map<String,Object> e : effectData) {
            String type = ((String)e.get("type")).toUpperCase(Locale.ROOT);

            registry.put(type, data ->  // Function<Map, EffectDescriptor>
                    (src, tgt) -> new SimpleEffect(
                            (String)e.getOrDefault("name", type),
                            ((Number)e.getOrDefault("magnitude", 0)).intValue(),
                            ((Number)e.getOrDefault("duration", 0)).intValue()
                    ));
        }
    }

    public EffectDescriptor getDescriptor(String type) {
        Function<Map<String,Object>, EffectDescriptor> func = registry.get(type.toUpperCase(Locale.ROOT));
        if (func == null)
            throw new IllegalArgumentException("Unknown effect type: " + type);
        // a default empty map creates default magnitudes
        return func.apply(Collections.emptyMap());
    }

    public Effect create(String type, Combatant src, Combatant tgt, Map<String,Object> data) {
        Function<Map<String,Object>, EffectDescriptor> func = registry.get(type.toUpperCase(Locale.ROOT));
        if (func == null)
            throw new IllegalArgumentException("Unknown effect type: " + type);
        return func.apply(data).instantiate(src, tgt);
    }
}