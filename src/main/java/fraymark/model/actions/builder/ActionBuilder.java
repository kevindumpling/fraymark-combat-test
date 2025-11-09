package fraymark.model.actions.builder;

import fraymark.model.actions.*;
import fraymark.model.actions.physical.BasicPhysicalAction;
import fraymark.model.actions.weaves.WeaveAction;
import fraymark.combat.damage.pipeline.DamagePipeline;
import java.util.*;
import java.util.function.Function;

public class ActionBuilder {
    private final Map<String, Function<Map<String, Object>, Action>> registry = new HashMap<>();

    public ActionBuilder() {
    }

    public Action buildFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Missing 'type' field in action entry: " + data);
        }

        String name = (String) data.getOrDefault("name", "Unnamed Action");
        int power = ((Number) data.getOrDefault("power", 0)).intValue();
        int trpCost = ((Number) data.getOrDefault("trpCost", 0)).intValue();
        System.out.println("Building action: " + data.get("id") + " (" + type + ")");

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "PHYSICAL_BASIC" -> new BasicPhysicalAction(name, power);
            case "WEAVE" -> new WeaveAction(name, power, trpCost);
            case "WEAPON" -> new BasicPhysicalAction(name, power);

            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }
}