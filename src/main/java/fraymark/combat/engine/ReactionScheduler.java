package fraymark.combat.engine;

import fraymark.model.combatants.Combatant;
import fraymark.model.actions.Action;
import java.util.*;

/***
 * Creates a priority order of InterruptPhases based on how high the SPD of the actor was
 * to resolve in SPD order.
 */
public class ReactionScheduler {
    private final PriorityQueue<InterruptState> queue;

    public ReactionScheduler() {
        queue = new PriorityQueue<>(Comparator.comparingInt(
                s -> -s.getInterrupter().getStats().getSpd() // SPD = reaction speed
        ));
    }

    public void schedule(Combatant source, Action action, List<Combatant> targets) {
        queue.add(new InterruptState(source, action, targets));
    }

    public InterruptState next() { return queue.poll(); }
    public boolean hasNext() { return !queue.isEmpty(); }
    public void clear() { queue.clear(); }
}