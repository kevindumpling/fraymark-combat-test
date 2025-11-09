package fraymark.model.combatants;

import java.util.List;

/***
 * A Party is a collection of Player-controllable Combatants.
 */
public class Party {
    private final List<Combatant> members;

    public Party(List<Combatant> members) { this.members = members; }
    public List<Combatant> getMembers() { return members; }
}
