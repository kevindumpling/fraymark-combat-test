package fraymark.model.weapons;

import fraymark.model.actions.Action;
import java.util.*;

/**
 * Factory that builds weapons from data. <br>
 * Can be expanded to load JSON or XML later.
 */
public class WeaponFactory {
    private final Map<String, Weapon> registry = new HashMap<>();

    public void registerWeapon(String id, Weapon weapon) {
        registry.put(id, weapon);
    }

    public Weapon createWeapon(String id) {
        Weapon template = registry.get(id);
        if (template == null) return null;
        // shallow copy with shared actions
        return new Weapon(id, template.getName(), template.getType(), template.getGrantedActions());
    }

    public static WeaponFactory defaultFactory() {
        WeaponFactory f = new WeaponFactory();
        // Example: f.registerWeapon("knife-basic", new Weapon("knife-basic", "Combat Knife", WeaponType.KNIFE, List.of()));
        return f;
    }
}