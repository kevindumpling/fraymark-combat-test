package fraymark.model.fields.factory;

import fraymark.model.effects.factory.EffectFactory;
import fraymark.model.fields.Field;
import fraymark.model.fields.FieldDescriptor;
import fraymark.model.fields.impl.AuraField;
import fraymark.model.fields.impl.SchoolModifierField;

import java.util.*;
import java.util.function.Function;


public class FieldFactory {
    // type -> (data -> descriptor)
    private final Map<String, Function<Map<String,Object>, FieldDescriptor>> registry = new HashMap<>();
    // id -> raw template data from fields.json
    private final Map<String, Map<String,Object>> templatesById = new HashMap<>();
    private final EffectFactory effectFactory;

    public FieldFactory(EffectFactory effectFactory) {
        this.effectFactory = effectFactory;
        // register core field types here:
        // e.g. SCHOOL_MODIFIER, TRP_MODIFIER, AURA, etc.
        this.registerCoreTypes();

    }

    private void registerCoreTypes(){
        registerType("SCHOOL_MODIFIER", data -> state -> {
            String id = (String) data.getOrDefault("id", "field");
            String name = (String) data.getOrDefault("name", id);
            String desc = (String) data.getOrDefault("description", "");
            int dur = (int) data.getOrDefault("duration", 0);

            Map<String, Double> schoolMul = new HashMap<>();
            Object sm = data.get("schoolMultipliers");
            if (sm instanceof Map<?,?> m) {
                m.forEach((k, v) -> {
                    if (k != null && v instanceof Number n) {
                        schoolMul.put(k.toString().toUpperCase(Locale.ROOT), n.doubleValue());
                    }
                });
            }

            Map<String, Double> weaveMul = new HashMap<>();
            Object wm = data.get("weaveMultipliers");
            if (sm instanceof Map<?,?> m) {
                m.forEach((k, v) -> {
                    if (k != null && v instanceof Number n) {
                        schoolMul.put(k.toString().toUpperCase(Locale.ROOT), n.doubleValue());
                    }
                });
            }

            double physicalMul = asDouble(data.get("physicalDamageMul"), 1.0);

            return new SchoolModifierField(id, name, desc, dur, schoolMul, weaveMul);
        });

        registerType("AURA", data -> state -> {
            String id   = (String) data.getOrDefault("id", "aura");
            String name = (String) data.getOrDefault("name", id);
            String desc = (String) data.getOrDefault("description", "");
            int interval = asInt(data.get("interval"), 1);
            String targets = (String) data.getOrDefault("targets", "ALL_COMBATANTS");
            String effectId = (String) data.get("effectId");

            return new AuraField(id, name, desc, interval, targets, effectId, this.effectFactory);
        });
    }

    public void registerType(String type, Function<Map<String,Object>, FieldDescriptor> ctor) {
        registry.put(type.toUpperCase(Locale.ROOT), ctor);
    }

    public void registerTemplate(String id, Map<String,Object> data) {
        templatesById.put(id.toLowerCase(Locale.ROOT), new HashMap<>(data));
    }

    public FieldDescriptor descriptorFromId(String id) {
        return descriptorFromId(id, Collections.emptyMap());
    }

    public FieldDescriptor descriptorFromId(String id, Map<String,Object> overrides) {
        Map<String,Object> base = templatesById.get(id.toLowerCase(Locale.ROOT));
        if (base == null) {
            System.err.println("Unknown field id: " + id);
            return state -> new Field() {
                @Override public String id() { return "missing:" + id; }
                @Override public String getName() { return "Missing Field"; }
                @Override public String getDescription() { return "Error"; }
            };
        }
        Map<String,Object> merged = new HashMap<>(base);
        merged.putAll(overrides);

        Object typeObj = merged.get("type");
        if (!(typeObj instanceof String type)) {
            throw new IllegalArgumentException("Field template " + id + " missing 'type'");
        }

        Function<Map<String,Object>, FieldDescriptor> ctor =
                registry.get(type.toUpperCase(Locale.ROOT));
        if (ctor == null) {
            throw new IllegalArgumentException("Unknown field type: " + type);
        }

        return ctor.apply(merged);
    }

    private static double asDouble(Object v, double def) {
        if (v instanceof Number n) return n.doubleValue();
        try { return v != null ? Double.parseDouble(v.toString()) : def; } catch (Exception e) { return def; }
    }

    private static int asInt(Object v, int def) {
        if (v instanceof Number n) return n.intValue();
        try { return v != null ? Integer.parseInt(v.toString()) : def; } catch (Exception e) { return def; }
    }
}
