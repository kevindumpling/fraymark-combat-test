package fraymark.data;

import fraymark.combat.engine.BattleState;
import fraymark.model.actions.Action;
import fraymark.model.actions.builder.ActionBuilder;
import fraymark.model.effects.factory.EffectFactory;
import fraymark.model.weapons.*;
import fraymark.model.combatants.*;
import java.util.*;

/**
 * Coordinates loading and assembling all gameplay data.
 * JSON → Maps → Factories → Live Model Objects
 */
public class DataAssembler {
    private final EffectFactory effectFactory = new EffectFactory();
    private final ActionBuilder actionBuilder = new ActionBuilder(effectFactory);
    private final WeaponFactory weaponFactory = new WeaponFactory();
    private final CharacterFactory characterFactory = new CharacterFactory();

    private final Map<String, Action> actionRegistry = new HashMap<>();
    private final Map<String, Weapon> weaponRegistry = new HashMap<>();

    /** ===== ENTRY POINT ===== */
    public BattleState buildFullBattle() {
        loadAllData();

        // Build party + enemies
        List<Combatant> party = buildCharacters("party.json");
        List<Combatant> enemies = buildCharacters("enemies.json");


        return new BattleState(party, enemies);
    }

    /** ===== MASTER DATA LOAD ===== */
    public void loadAllData() {
        // Load all JSON data
        var effectsData  = DataLoader.loadEffectsRaw();
        var weavesData   = DataLoader.loadWeavesRaw();
        var physicalsData = DataLoader.loadPhysicalsRaw();
        var weaponsData  = DataLoader.loadWeaponsRaw();

        // Register effects
        effectFactory.loadFromTypeDefs(effectsData);

        System.out.println("DEBUG: Physicals raw: " + physicalsData.size() + " entries");
        if (!physicalsData.isEmpty()) {
            System.out.println("    DEBUG: First physical entry keys: " + physicalsData.get(0).keySet());
        }

        // === Build all actions (weaves + physicals) ===
        List<Map<String, Object>> allActions = new ArrayList<>();
        allActions.addAll(weavesData);
        allActions.addAll(physicalsData);

        for (Map<String, Object> actionEntry : allActions) {
            Action action = actionBuilder.buildFromData(actionEntry);
            actionRegistry.put((String) actionEntry.get("id"), action);
            System.out.println("DEBUG: Loaded action to live model: " + actionEntry.get("id") + " (" + action.getType() + ")");

        }

        System.out.println("=== DEBUG: Action Registry Contents ===");
        for (String key : actionRegistry.keySet()) {
            System.out.println(" -> " + key);
        }
        System.out.println("===============================");

        // === Build weapons and connect actions ===
        weaponFactory.loadFromData(weaponsData, actionBuilder, actionRegistry);
        weaponRegistry.putAll(weaponFactory.getRegistry());
    }

    /** ===== CHARACTER LOADERS ===== */
    public List<Combatant> buildCharacters(String fileName) {
        try {
            var charsData = DataLoader.loadCharactersRaw(fileName);
            return characterFactory.buildCharacters(charsData, weaponRegistry, actionRegistry);
        } catch (RuntimeException ex) {
            return List.of(); // silently skip if missing
        }
    }

    /** ===== ACCESSORS ===== */
    public Map<String, Action> getActionRegistry() { return actionRegistry; }
    public Map<String, Weapon> getWeaponRegistry() { return weaponRegistry; }
    public EffectFactory getEffectFactory() { return effectFactory; }
}