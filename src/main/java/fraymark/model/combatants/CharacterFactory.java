package fraymark.model.combatants;

import fraymark.model.actions.Action;
import fraymark.model.stats.Resources;
import fraymark.model.stats.Stats;
import fraymark.model.weapons.Weapon;
import java.util.*;
import java.util.function.Function;

public class CharacterFactory {

    private final Map<String, Function<Map<String, Object>, Combatant>> registry = new HashMap<>();

    public CharacterFactory() {
        // Register known types
        register("PLAYER", data -> {
            String id = (String) data.get("id");
            String name = (String) data.getOrDefault("name", "Unnamed Player");
            boolean armored = Boolean.TRUE.equals(data.get("armored"));
            int maxHP = ((Number) data.getOrDefault("maxHP", 100)).intValue();
            int atk = ((Number) data.getOrDefault("atk", 10)).intValue();
            int def = ((Number) data.getOrDefault("def", 10)).intValue();
            int wil = ((Number) data.getOrDefault("wil", 10)).intValue();
            int res = ((Number) data.getOrDefault("res", 10)).intValue();
            int spd = ((Number) data.getOrDefault("spd", 10)).intValue();

            Stats s = new Stats(maxHP, atk, def, wil, res, spd);
            Resources r = new Resources(maxHP, 0, 0, 0, 0);
            return new PlayerCharacter(id, name, s, r, armored);
        });

        register("ENEMY", data -> {
            String id = (String) data.get("id");
            String name = (String) data.getOrDefault("name", "Unnamed Enemy");
            boolean armored = Boolean.TRUE.equals(data.get("armored"));
            int maxHP = ((Number) data.getOrDefault("maxHP", 100)).intValue();
            int atk = ((Number) data.getOrDefault("atk", 10)).intValue();
            int def = ((Number) data.getOrDefault("def", 10)).intValue();
            int wil = ((Number) data.getOrDefault("wil", 10)).intValue();
            int res = ((Number) data.getOrDefault("res", 10)).intValue();
            int spd = ((Number) data.getOrDefault("spd", 10)).intValue();

            Stats s = new Stats(maxHP, atk, def, wil, res, spd);
            Resources r = new Resources(maxHP, 0, 0, 0, 0);
            return new Enemy(id, name, s, r, armored, 1);
        });
    }

    private void register(String type, Function<Map<String, Object>, Combatant> constructor) {
        registry.put(type.toUpperCase(Locale.ROOT), constructor);
    }

    @SuppressWarnings("unchecked")
    public List<Combatant> buildCharacters(List<Map<String, Object>> charData,
                                           Map<String, Weapon> weaponRegistry,
                                           Map<String, Action> actionRegistry) {
        List<Combatant> result = new ArrayList<>();
        for (Map<String, Object> c : charData) {
            String type = ((String) c.getOrDefault("type", "PLAYER")).toUpperCase(Locale.ROOT);
            Function<Map<String, Object>, Combatant> ctor = registry.get(type);
            if (ctor == null)
                throw new IllegalArgumentException("Unknown character type: " + type);

            Combatant combatant = ctor.apply(c);

            // Attach weapon
            String weaponId = (String) c.get("weapon");
            if (weaponId != null && weaponRegistry.containsKey(weaponId))
                combatant.setWeapon(weaponRegistry.get(weaponId));

            // Attach actions
            List<String> actionIds = (List<String>) c.getOrDefault("actions", List.of());
            for (String aId : actionIds)
                if (actionRegistry.containsKey(aId))
                    combatant.addAction(actionRegistry.get(aId));

            result.add(combatant);
            System.out.println("Built character: " + combatant.getName() + " (" + type + ")");

        }
        return result;
    }
}
