package fraymark.model.weapons;

import fraymark.model.actions.Action;
import fraymark.model.actions.builder.ActionBuilder;
import java.util.*;

public class WeaponFactory {
    private final Map<String, Weapon> weaponRegistry = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void loadFromData(List<Map<String, Object>> weaponData,
                             ActionBuilder actionBuilder,
                             Map<String, Action> globalActionRegistry) {
        for (Map<String, Object> w : weaponData) {
            String id = (String) w.get("id");
            String name = (String) w.getOrDefault("name", "Unnamed Weapon");
            WeaponType weaponClass = WeaponType.fromString(w.get("weapon_class"));
            int attack = ((Number) w.getOrDefault("attack", 10)).intValue();
            int accuracy = ((Number) w.getOrDefault("accuracy", 80)).intValue();

            Weapon weapon = new Weapon(id, name, weaponClass, new ArrayList<>());

            Object maybeActions = w.get("actions");
            if (maybeActions instanceof List<?> list) {
                for (Object a : list) {
                    if (a instanceof String aId) {
                        Action action = globalActionRegistry.get(aId);
                        if (action != null) weapon.addAction(action);
                    } else if (a instanceof Map<?,?> actionMap) {
                        Action action = actionBuilder.buildFromData((Map<String,Object>) actionMap);
                        weapon.addAction(action);
                    }
                }
            }

            weaponRegistry.put(id, weapon);
        }
    }

    public Map<String, Weapon> getRegistry() { return weaponRegistry; }
}
