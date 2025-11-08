package fraymark.model.actions.builder;

import fraymark.model.actions.*;
import fraymark.model.actions.physical.*;
import fraymark.model.actions.weaves.*;
import java.util.Map;

/**
 * Converts JSON maps into Action objects.
 */
public class ActionBuilder {

    public Action buildFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        String name = (String) data.get("name");
        int power = ((Number) data.getOrDefault("power", 0)).intValue();
        int trpCost = ((Number) data.getOrDefault("trpCost", 0)).intValue();

        return switch (type.toUpperCase()) {
            // Example: case "BASIC_PHYSICAL" -> new BasicPhysical(name, power);
            case "WEAVE" -> null;  // PLACEHOLDER: TODO
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }
}