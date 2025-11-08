package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import java.util.List;

/***
 * Handles actions related to the current state of battle, such as getting the current actor, incrementing the next turn,
 * and getting which phase of battle is active.
 */
public class BattleState {
    private final List<Combatant> party;
    private final List<Combatant> enemies;
    private int currentIndex = 0;
    private TurnPhase currentPhase = TurnPhase.PLAYER_TURN;

    public BattleState(List<Combatant> party, List<Combatant> enemies) {
        this.party = party;
        this.enemies = enemies;
    }

    public List<Combatant> getParty() { return party; }
    public List<Combatant> getEnemies() { return enemies; }

    public Combatant getCurrentActor() {
        return currentPhase == TurnPhase.PLAYER_TURN
                ? party.get(currentIndex % party.size())
                : enemies.get(currentIndex % enemies.size());
    }

    public void nextTurn() { currentIndex++; }
    public TurnPhase getPhase() { return currentPhase; }
    public void setPhase(TurnPhase phase) { this.currentPhase = phase; }
}