package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import fraymark.model.position.Formation;
import fraymark.model.position.Lineup;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles the current state of battle with proper turn order based on SPD.
 */
public class BattleState {
    private final List<Combatant> party;
    private final List<Combatant> enemies;
    private List<Combatant> turnOrder;
    private int currentTurnIndex = 0;
    private int roundNumber = 1;
    private TurnPhase currentPhase = TurnPhase.PLAYER_TURN;

    private Formation partyFormation = Formation.KNOT;
    private Formation enemyFormation = Formation.KNOT;
    private Lineup partyLineup, enemyLineup;

    public BattleState(List<Combatant> party, List<Combatant> enemies) {
        this.party = party;
        this.enemies = enemies;
        this.turnOrder = new ArrayList<>();
        initializeTurnOrder();

        this.partyLineup = new Lineup(party, partyFormation);
        this.enemyLineup = new Lineup(enemies, enemyFormation);


    }

    /**
     * Initialize turn order based on SPD stat (highest SPD goes first).
     */
    private void initializeTurnOrder() {
        turnOrder = Stream.concat(party.stream(), enemies.stream())
                .filter(c -> c.getResources().getHp() > 0) // Only alive combatants
                .sorted((a, b) -> Integer.compare(
                        b.getStats().getSpd(),
                        a.getStats().getSpd()
                ))
                .collect(Collectors.toList());

        currentTurnIndex = 0;
        System.out.println("=== Turn Order Initialized ===");
        for (int i = 0; i < turnOrder.size(); i++) {
            Combatant c = turnOrder.get(i);
            System.out.println((i + 1) + ". " + c.getName() +
                    " (SPD: " + c.getStats().getSpd() + ")");
        }
    }

    /**
     * Get the combatant whose turn it currently is.
     */
    public Combatant getCurrentActor() {
        if (turnOrder.isEmpty()) {
            return null;
        }

        // Skip dead combatants
        while (currentTurnIndex < turnOrder.size()) {
            Combatant current = turnOrder.get(currentTurnIndex);
            if (current.getResources().getHp() > 0) {
                updatePhase(current);
                return current;
            }
            currentTurnIndex++;
        }

        // If we've gone through everyone, start a new round
        if (currentTurnIndex >= turnOrder.size()) {
            startNewRound();
            return getCurrentActor();
        }

        return null;
    }

    /**
     * Update the current phase based on who's acting.
     */
    private void updatePhase(Combatant actor) {
        if (party.contains(actor)) {
            currentPhase = TurnPhase.PLAYER_TURN;
        } else if (enemies.contains(actor)) {
            currentPhase = TurnPhase.ENEMY_TURN;
        }
    }

    /**
     * Advance to the next turn.
     */
    public void nextTurn() {
        currentTurnIndex++;

        // Give the baseline TRP gain. TODO: make this work with trp mechanics
        for (Combatant combatant: turnOrder){
            combatant.getResources().setTrp(combatant.getResources().getTrp() + 1);
        }

        // Check if round is complete
        if (currentTurnIndex >= turnOrder.size()) {
            startNewRound();
        }

        System.out.println("Turn advanced. Current index: " + currentTurnIndex +
                " / " + turnOrder.size());
    }

    public void initLineups() {
        this.partyLineup = new Lineup(party, partyFormation);
        this.enemyLineup = new Lineup(enemies, enemyFormation);
    }

    /**
     * Start a new round (refresh turn order, increment round counter).
     */
    private void startNewRound() {
        roundNumber++;
        currentTurnIndex = 0;

        // Rebuild turn order (in case combatants died)
        initializeTurnOrder();

        System.out.println("=== Round " + roundNumber + " Started ===");
    }

    /**
     * Check if the battle is over.
     */
    public boolean isBattleOver() {
        boolean partyAlive = party.stream().anyMatch(c -> c.getResources().getHp() > 0);
        boolean enemiesAlive = enemies.stream().anyMatch(c -> c.getResources().getHp() > 0);
        return !partyAlive || !enemiesAlive;
    }

    /**
     * Get battle result.
     */
    public BattleResult getResult() {
        boolean partyAlive = party.stream().anyMatch(c -> c.getResources().getHp() > 0);
        boolean enemiesAlive = enemies.stream().anyMatch(c -> c.getResources().getHp() > 0);

        if (!partyAlive) {
            return BattleResult.DEFEAT;
        } else if (!enemiesAlive) {
            return BattleResult.VICTORY;
        } else {
            return BattleResult.ONGOING;
        }
    }


    // === Getters ===
    public List<Combatant> getParty() { return party; }
    public List<Combatant> getEnemies() { return enemies; }
    public List<Combatant> getTurnOrder() { return turnOrder; }
    public int getCurrentTurnIndex() { return currentTurnIndex; }
    public int getRoundNumber() { return roundNumber; }
    public TurnPhase getPhase() { return currentPhase; }
    public void setPhase(TurnPhase phase) { this.currentPhase = phase; }

    public Lineup getPartyLineup() { return partyLineup; }
    public Lineup getEnemyLineup() { return enemyLineup; }
    public Formation getPartyFormation() { return partyFormation; }
    public Formation getEnemyFormation() { return enemyFormation; }
    public void setPartyFormation(Formation f) { partyFormation = f; initLineups(); }
    public void setEnemyFormation(Formation f) { enemyFormation = f; initLineups(); }

    /**
     * Battle result enum.
     */
    public enum BattleResult {
        ONGOING,
        VICTORY,
        DEFEAT
    }
}