package fraymark.model.effects.factory;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;

import java.util.*;
import java.util.function.Function;

/***
 * All Effects must be registered here for creation.
 */
public class EffectFactory {
    // type -> constructor that consumes a data map and returns a descriptor
    private final Map<String, Function<Map<String, Object>, EffectDescriptor>> registry = new HashMap<>();

    public EffectFactory() {
        // Register effects.
        register("BLEED", data -> (src, tgt) -> new SimpleEffect(
                (String) data.getOrDefault("name", "Bleed"),
                EffectType.BLEED,
                ((Number) data.getOrDefault("magnitude", 5)).intValue(),
                ((Number) data.getOrDefault("duration", 2)).intValue()
        ));

        register("BURN", data -> (src, tgt) -> new SimpleEffect(
                (String) data.getOrDefault("name", "Burn"),
                EffectType.BURN,
                ((Number) data.getOrDefault("magnitude", 4)).intValue(),
                ((Number) data.getOrDefault("duration", 3)).intValue()
        ));

        register("DEF_DOWN", data -> (src, tgt) -> new SimpleEffect(
                (String) data.getOrDefault("name", "Defense Down"),
                EffectType.DEF_DOWN,
                ((Number) data.getOrDefault("magnitude", 10)).intValue(),
                ((Number) data.getOrDefault("duration", 2)).intValue()
        ));

        register("RES_DOWN", data -> (src, tgt) -> new SimpleEffect(
                (String) data.getOrDefault("name", "Resistance Down"),
                EffectType.RES_DOWN,
                ((Number) data.getOrDefault("magnitude", 10)).intValue(),
                ((Number) data.getOrDefault("duration", 2)).intValue()
        ));

    }

    public void register(String type, Function<Map<String,Object>, EffectDescriptor> constructor) {
        registry.put(type.toUpperCase(Locale.ROOT), constructor);
    }

    /**
     * Return an EffectDescriptor for a given type using the supplied parameters.
     * Used by ActionBuilder to attach descriptors to actions (no src/tgt yet).
     */
    public EffectDescriptor descriptor(String type, Map<String,Object> data) {
        Function<Map<String,Object>, EffectDescriptor> func = registry.get(type.toUpperCase(Locale.ROOT));
        if (func == null)
            throw new IllegalArgumentException("Unknown effect type: " + type);
        return func.apply(data != null ? data : Collections.emptyMap());
    }

    /**
     * Convenience: directly instantiate an Effect for (src, tgt) with the given type/data.
     * (Most action flows should prefer 'descriptor(...)' and instantiate later.)
     */
    public Effect create(String type, Combatant src, Combatant tgt, Map<String,Object> data) {
        return descriptor(type, data).instantiate(src, tgt);
    }

    /**
     * Optional bulk registration of custom types. Unlike your previous version,
     * this stores constructors that read the 'data' passed at USE time, not at LOAD time.
     * Example entries look like: {"type":"BLEED","name":"Frostbite","magnitude":6,"duration":2}
     */
    public void loadFromTypeDefs(List<Map<String,Object>> typeDefs) {
        for (Map<String,Object> def : typeDefs) {
            Object t = def.get("type");
            if (!(t instanceof String)) continue;
            String type = ((String) t).toUpperCase(Locale.ROOT);

            // create a constructor that reads from the USE-time data map,
            // falling back to defaults captured from the typeDef (optional)
            final String defaultName = (String) def.getOrDefault("name", type);
            final int defaultMag = ((Number) def.getOrDefault("magnitude", 0)).intValue();
            final int defaultDur = ((Number) def.getOrDefault("duration", 0)).intValue();

            // Pick effect kind. If you keep EffectType, map it here:
            final EffectType effectKind = EffectType.valueOf(type); // BLEED -> EffectType.BLEED, etc.

            register(type, data -> (src, tgt) -> new SimpleEffect(
                    (String) data.getOrDefault("name", defaultName),
                    effectKind,
                    ((Number) data.getOrDefault("magnitude", defaultMag)).intValue(),
                    ((Number) data.getOrDefault("duration", defaultDur)).intValue()
            ));
        }
    }
}
