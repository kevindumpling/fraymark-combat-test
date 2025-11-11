package fraymark.model.actions.builder;

import fraymark.model.actions.*;
import fraymark.model.actions.physical.BasicPhysicalAction;
import fraymark.model.actions.weaves.WeaveAction;
import fraymark.model.effects.EffectDescriptor;
import fraymark.model.effects.factory.EffectFactory;

import java.util.*;

public class ActionBuilder {
    private final EffectFactory effectFactory;

    public ActionBuilder(EffectFactory effectFactory) {this.effectFactory = effectFactory; }

    @SuppressWarnings("unchecked")
    public Action buildFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Missing 'type' field in action entry: " + data);
        }

        String name = (String) data.getOrDefault("name", "Unnamed Action");
        int power = ((Number) data.getOrDefault("power", 0)).intValue();
        int trpCost = ((Number) data.getOrDefault("trpCost", 0)).intValue();
        String flavorOnUse = (String) data.getOrDefault("flavorOnUse", "");
        String targeting = (String) data.getOrDefault("targeting", "SINGLE");
        String rangeKind = (String) data.getOrDefault("rangeKind", "ALL");

        System.out.println("Building action: " + data.get("id") + " (" + type + ")");

        // 1) Construct the action instance
        Action action = switch (type.toUpperCase(Locale.ROOT)) {
            case "PHYSICAL_BASIC", "WEAPON" -> new BasicPhysicalAction(name, power, trpCost, flavorOnUse,  TargetingMode.valueOf(targeting), AttackRangeKind.valueOf(rangeKind));
            case "WEAVE" -> new WeaveAction(name, power, trpCost, flavorOnUse, TargetingMode.valueOf(targeting), AttackRangeKind.valueOf(rangeKind));
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };

        // Parse optional effects: a list of objects like
        //    { "type": "BLEED", "magnitude": 6, "duration": 2, "name": "Frostbite" }
        Object rawEffects = data.get("effects");
        if (rawEffects instanceof List<?> list && !list.isEmpty()) {
            for (Object o : list) {
                if (!(o instanceof Map)) {
                    System.err.println("Skipping effect entry (not an object): " + o);
                    continue;
                }
                Map<String, Object> eData = (Map<String, Object>) o;
                String eType = (String) eData.get("type");
                if (eType == null || eType.isBlank()) {
                    System.err.println("Skipping effect entry (missing 'type'): " + eData);
                    continue;
                }

                // create a descriptor with parameters (no src/tgt yet)
                EffectDescriptor desc = effectFactory.descriptor(eType, eData);

                // attach to the action (requires an adder on the concrete action)
                if (action instanceof WeaveAction wa) {
                    wa.addEffectDescriptor(desc);
                } else if (action instanceof BasicPhysicalAction pa) {
                    pa.addEffectDescriptor(desc);
                } else {
                    // If you add more Action types later, handle them here
                    System.err.println("Action type lacks effectBundle adder: " + action.getClass());
                }
            }
        }

        return action;
    }
}
