package fraymark.model.combatants;

import fraymark.model.stats.Stats;
import fraymark.model.stats.Resources;

/**
 * An Enemy is a Combatant controlled by AI.
 */
public class Enemy extends Combatant {
    private int aiLevel;

    public Enemy(String id, String name, Stats stats, Resources res, boolean armored, int aiLevel) {
        super(id, name, stats, res, armored, false);
        this.aiLevel = aiLevel;
    }

    public int getAiLevel() { return aiLevel; }
    public void setAiLevel(int aiLevel) { this.aiLevel = aiLevel; }
}