package fraymark.model.fields.impl;
import fraymark.combat.engine.BattleState;
import fraymark.combat.events.CombatEvent;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.factory.EffectFactory;
import fraymark.model.fields.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuraField implements Field {
    private final String id;
    private final String name;
    private final String description;
    private final int interval;
    private final String targets;   // e.g., ALL_ALLIES, ALL_ENEMIES, ALL_COMBATANTS
    private final String effectId;
    private int turnCounter = 0;

    private final fraymark.model.effects.factory.EffectFactory effectFactory;

    public AuraField(String id, String name, String description,
                     int interval, String targets, String effectId,
                     EffectFactory effectFactory) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.interval = interval;
        this.targets = targets;
        this.effectId = effectId;
        this.effectFactory = effectFactory;
    }

    @Override public String id() { return id; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }

    @Override
    public List<CombatEvent> onTurnEnd(BattleState state) {
        turnCounter++;
        System.out.println("[AuraField] onTurnEnd called; turnCounter=" + turnCounter);

        if (interval <= 0 || (turnCounter % interval) != 0) return List.of();

        List<Combatant> targetsList = switch (targets.toUpperCase(Locale.ROOT)) {
            case "ALL_ALLIES"       -> state.getParty();
            case "ALL_ENEMIES"      -> state.getEnemies();
            case "ALL_COMBATANTS"   -> {
                List<Combatant> all = new ArrayList<>(state.getParty());
                all.addAll(state.getEnemies());
                yield all;
            }
            default -> List.of();
        };

        List<CombatEvent> out = new ArrayList<>();
        var desc = effectFactory.descriptorFromId(effectId);
        for (Combatant t : targetsList) {
            var effect = desc.instantiate(null, t); // no specific source or a "field" pseudo-source
            out.add(CombatEvent.applyEffect(
                    null, // or a special FieldCombatant
                    t,
                    effect,
                    t.getName() + " is affected by " + effect.getName() + " from " + name + "!"
            ));
        }
        return out;
    }
}

