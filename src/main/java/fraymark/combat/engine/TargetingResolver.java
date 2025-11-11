// fraymark/combat/engine/TargetingResolver.java
package fraymark.combat.engine;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;
import fraymark.model.position.*;
import java.util.*;

public class TargetingResolver {
    public static List<Combatant> resolveTargets(BattleState state, Action action, Combatant clicked) {
        if (clicked == null) return List.of();
        if (action == null)  return List.of(clicked);

        TargetingMode mode = action.getTargeting();
        if (mode == null) mode = TargetingMode.SINGLE;

        boolean ally = state.getParty().contains(clicked);
        Lineup lineup = ally ? state.getPartyLineup() : state.getEnemyLineup();
        DistanceTier tier = lineup.tierOf(clicked);

        List<Combatant> raw = switch (mode) {
            case SINGLE -> List.of(clicked);
            case AOE_TIER, AOE_ALL_TIER_SIDE -> lineup.ofTier(tier);
            case AOE_ALL -> (ally ? state.getParty() : state.getEnemies());
        };

        return raw.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getResources().getHp() > 0)
                .distinct()
                .toList();
    }

}
