package fraymark.model.fields;

import fraymark.combat.engine.BattleState;

@FunctionalInterface
public interface FieldDescriptor {
    Field instantiate(BattleState state);
}
