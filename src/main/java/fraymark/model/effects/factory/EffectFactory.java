// src/main/java/fraymark/model/effects/factory/EffectFactory.java
package fraymark.model.effects.factory;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;

import java.util.*;
import java.util.function.Function;
import fraymark.model.effects.impl.*;

public class EffectFactory {
    // type -> constructor(data) -> EffectDescriptor
    private final Map<String, Function<Map<String, Object>, EffectDescriptor>> registry = new HashMap<>();

    public EffectFactory() {
        // --- SIMPLE (generic) ---
        // JSON example:
        // { "type":"SIMPLE", "name":"Bleed", "effectType":"BLEED", "magnitude":8, "duration":3,
        //   "rollRateMultiplier":1.5, "stacking":"STACK_ADD" }
        register("SIMPLE", data -> (src, tgt) -> {
            String name = (String) data.getOrDefault("name", "Effect");
            EffectType kind = parseEnum(data.get("effectType"), EffectType.class, EffectType.BLEED);
            int mag = asInt(data.get("magnitude"), 0);
            int dur = asInt(data.get("duration"), 1);

            double rrm = asDouble(data.get("rollRateMultiplier"), 1.0);
            StackingRule stacking = parseEnum(data.get("stacking"), StackingRule.class, StackingRule.REFRESH_DURATION);

            // Anonymous subclass to override metadata hooks (stacking, roll-rate, key if desired)
            return new SimpleEffect(name, kind, mag, dur) {
                @Override public StackingRule stacking() { return stacking; }
                @Override public double rollRateMultiplier() { return rrm; }
                // Optional: provide a more specific key so different magnitudes don't collide
                @Override public String key() { return kind.name() + ":" + mag; }
            };
        });

        // --- BLEED / BURN / DEF_DOWN / RES_DOWN convenience types (back-compat) ---
        // Still available for your existing data files. They also accept optional rollRateMultiplier/stacking.
        registerSimpleShortcut("BLEED",  "Bleed",      EffectType.BLEED,    5, 2);
        registerSimpleShortcut("BURN",   "Burn",       EffectType.BURN,     4, 3);
        registerSimpleShortcut("DEF_DOWN","Defense Down",EffectType.DEF_DOWN,10, 2);
        registerSimpleShortcut("RES_DOWN","Resistance Down",EffectType.RES_DOWN,10, 2);

        // --- BARRIER timed buff ---
        // JSON example:
        // { "type":"BARRIER", "name":"Psi Guard", "pct":0.30, "duration":3, "stacking":"REFRESH_DURATION" }
        register("BARRIER", data -> (src, tgt) -> {
            String name = (String) data.getOrDefault("name", "Barrier");
            double pct = clamp01(asDouble(data.get("pct"), 0.0));
            int dur = asInt(data.get("duration"), 1);
            StackingRule stacking = parseEnum(data.get("stacking"), StackingRule.class, StackingRule.REFRESH_DURATION);

            return new BarrierEffect(pct, dur) {
                @Override public String getName() { return name; }
                @Override public StackingRule stacking() { return stacking; }
                @Override public String key() { return "BARRIER:" + (int)(pct * 100); }
            };
        });

        // You can add more specialized effects here (e.g., TAUNT, MIGHT, REGEN, etc.)
    }

    public void register(String type, Function<Map<String,Object>, EffectDescriptor> constructor) {
        registry.put(type.toUpperCase(Locale.ROOT), constructor);
    }

    /** Return an EffectDescriptor for a given type using the supplied parameters. */
    public EffectDescriptor descriptor(String type, Map<String,Object> data) {
        Function<Map<String,Object>, EffectDescriptor> func = registry.get(type.toUpperCase(Locale.ROOT));
        if (func == null) throw new IllegalArgumentException("Unknown effect type: " + type);
        return func.apply(data != null ? data : Collections.emptyMap());
    }

    /** Convenience: directly instantiate an Effect for (src, tgt) with the given type/data. */
    public Effect create(String type, Combatant src, Combatant tgt, Map<String,Object> data) {
        return descriptor(type, data).instantiate(src, tgt);
    }

    /** Optional bulk loader: defines new shortcuts (like your old loadFromTypeDefs). */
    public void loadFromTypeDefs(List<Map<String,Object>> defs) {
        if (defs == null) return;
        for (Map<String,Object> def : defs) {
            Object tobj = def.get("type");
            if (!(tobj instanceof String)) continue;
            String type = ((String) tobj).toUpperCase(Locale.ROOT);

            // Capture defaults but still allow per-use overrides
            final String defaultName = (String) def.getOrDefault("name", type);
            final int defaultMag = asInt(def.get("magnitude"), 0);
            final int defaultDur = asInt(def.get("duration"), 1);
            final double defaultRrm = asDouble(def.get("rollRateMultiplier"), 1.0);
            final StackingRule defaultStack =
                    parseEnum(def.get("stacking"), StackingRule.class, StackingRule.REFRESH_DURATION);

            switch (type) {
                case "BARRIER" -> register("BARRIER", data -> (src, tgt) -> {
                    String name = (String) data.getOrDefault("name", defaultName);
                    double pct = clamp01(asDouble(data.getOrDefault("pct", def.get("pct")), 0.0));
                    int dur = asInt(data.getOrDefault("duration", defaultDur), defaultDur);
                    StackingRule stacking = parseEnum(
                            data.getOrDefault("stacking", defaultStack.name()),
                            StackingRule.class,
                            defaultStack
                    );
                    return new BarrierEffect(pct, dur) {
                        @Override public String getName() { return name; }
                        @Override public StackingRule stacking() { return stacking; }
                        @Override public String key() { return "BARRIER:" + (int)(pct * 100); }
                    };
                });
                case "SIMPLE", "BLEED", "BURN", "DEF_DOWN", "RES_DOWN" -> {
                    // For BLEED/BURN/DEF_DOWN/RES_DOWN without effectType, infer from type
                    register(type, data -> (src, tgt) -> {
                        String name = (String) data.getOrDefault("name", defaultName);
                        EffectType kind = type.equals("SIMPLE")
                                ? parseEnum(data.get("effectType"), EffectType.class, EffectType.BLEED)
                                : parseEnum(type, EffectType.class, EffectType.BLEED);
                        int mag = asInt(data.getOrDefault("magnitude", defaultMag), defaultMag);
                        int dur = asInt(data.getOrDefault("duration", defaultDur), defaultDur);
                        double rrm = asDouble(data.getOrDefault("rollRateMultiplier", defaultRrm), defaultRrm);
                        StackingRule stack = parseEnum(
                                data.getOrDefault("stacking", defaultStack.name()),
                                StackingRule.class, defaultStack);

                        return new SimpleEffect(name, kind, mag, dur) {
                            @Override public StackingRule stacking() { return stack; }
                            @Override public double rollRateMultiplier() { return rrm; }
                            @Override public String key() { return kind.name() + ":" + mag; }
                        };
                    });
                }
                default -> {
                    // Unknown custom type; you can throw or ignore
                    // throw new IllegalArgumentException("Unknown effect typedef: " + type);
                }
            }
        }
    }

    /* ----------------- helpers ----------------- */

    private static int asInt(Object v, int def) {
        if (v instanceof Number n) return n.intValue();
        try { return v != null ? Integer.parseInt(v.toString()) : def; } catch (Exception e) { return def; }
    }

    private static double asDouble(Object v, double def) {
        if (v instanceof Number n) return n.doubleValue();
        try { return v != null ? Double.parseDouble(v.toString()) : def; } catch (Exception e) { return def; }
    }

    private static double clamp01(double x) { return Math.max(0.0, Math.min(1.0, x)); }

    private static <E extends Enum<E>> E parseEnum(Object v, Class<E> cls, E def) {
        if (v == null) return def;
        if (cls.isInstance(v)) return cls.cast(v);
        try { return Enum.valueOf(cls, v.toString().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { return def; }
    }

    // Convenience to register classic shortcuts with optional overrides
    private void registerSimpleShortcut(String type, String defaultName,
                                        EffectType kind, int defaultMag, int defaultDur) {
        register(type, data -> (src, tgt) -> {
            String name = (String) data.getOrDefault("name", defaultName);
            int mag = asInt(data.get("magnitude"), defaultMag);
            int dur = asInt(data.get("duration"), defaultDur);
            double rrm = asDouble(data.get("rollRateMultiplier"), 1.0);
            StackingRule stacking = parseEnum(data.get("stacking"),
                    StackingRule.class, StackingRule.REFRESH_DURATION);

            return new SimpleEffect(name, kind, mag, dur) {
                @Override public StackingRule stacking() { return stacking; }
                @Override public double rollRateMultiplier() { return rrm; }
                @Override public String key() { return kind.name() + ":" + mag; }
            };
        });
    }
}
