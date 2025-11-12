package fraymark.model.combatants;

import fraymark.model.stats.Stats;
import fraymark.model.stats.Resources;

/**
 * A PlayerCharacter represents a character that recieves player input.
 * TODO: include inventory, experience, and dialogue bindings.
 */
public class PlayerCharacter extends Combatant {
    public PlayerCharacter(String id, String name, Stats stats, Resources res) {
        super(id, name, stats, res, true);
    }
}