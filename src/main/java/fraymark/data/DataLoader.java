package fraymark.data;

import fraymark.data.schemas.*;
import java.util.List;
import java.util.Map;

/***
 * DataLoader takes JSON information and the right Schema and turns it into understandable information for Factories.
 */
public class DataLoader {

    public static List<Map<String, Object>> loadActionsRaw() {
        return JsonUtils.readList("/fraymark/data/actions.json");
    }

    public static List<Map<String, Object>> loadEffectsRaw() {
        return JsonUtils.readList("/fraymark/data/effects.json");
    }

    public static List<Map<String, Object>> loadWeaponsRaw() {
        return JsonUtils.readList("/fraymark/data/weapons.json");
    }

    public static List<Map<String, Object>> loadCharactersRaw(String fileName) {
        return JsonUtils.readList("/fraymark/data/" + fileName);
    }

    public static List<Map<String, Object>> loadWeavesRaw() {
        return JsonUtils.readList("/fraymark/data/weaves.json");
    }

    public static List<Map<String, Object>> loadPhysicalsRaw() {
        return JsonUtils.readList("/fraymark/data/physicals.json");
    }
}