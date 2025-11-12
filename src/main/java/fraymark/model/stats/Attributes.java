package fraymark.model.stats;

import java.util.HashMap;
import java.util.Map;

/**
 * An Attribute class containing all stats pre-buff or pre-equip.
 * Useful for later seperation of base stats from the derived States.
 */
public class Attributes {
    private final Map<String, Integer> values = new HashMap<>();

    public void set(String key, int value) { values.put(key, value); }
    public int get(String key) { return values.getOrDefault(key, 0); }
    public Map<String, Integer> asMap() { return values; }

    public Stats toStats() {
        return new Stats(
                get("HP"),
                get("ATK"),
                get("DEF"),
                get("WIL"),
                get("RES"),
                get("SPD"),
                get("armorAmount")
        );
    }
}