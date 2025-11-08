package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import fraymark.model.actions.Action;
import java.util.List;

/**
 * Represents a temporary interruption phase that slots between TurnPhases.
 */
public class InterruptState {
    private final Combatant interrupter;
    private final Action action;
    private final List<Combatant> targets;

    public InterruptState(Combatant interrupter, Action action, List<Combatant> targets) {
        this.interrupter = interrupter;
        this.action = action;
        this.targets = targets;
    }

    public Combatant getInterrupter() { return interrupter; }
    public Action getAction() { return action; }
    public List<Combatant> getTargets() { return targets; }
}