// fraymark/model/position/Lineup.java
package fraymark.model.position;

import fraymark.model.combatants.Combatant;
import java.util.*;

public class Lineup {
    private final Map<Combatant, DistanceTier> tierMap = new HashMap<>();
    public Lineup(List<Combatant> members, Formation formation) {
        var tiers = formation.assign(members.size());
        for (int i = 0; i < members.size(); i++) tierMap.put(members.get(i), tiers.get(i));
    }
    public DistanceTier tierOf(Combatant c) { return tierMap.getOrDefault(c, DistanceTier.CLOSE); }
    public List<Combatant> ofTier(DistanceTier t) {
        return tierMap.entrySet().stream().filter(e->e.getValue()==t).map(Map.Entry::getKey).toList();
    }
    public Map<Combatant, DistanceTier> view() { return java.util.Collections.unmodifiableMap(tierMap); }
    public void setTier(Combatant c, DistanceTier t) { tierMap.put(c, t); } // (optional) dynamic shifts
}
