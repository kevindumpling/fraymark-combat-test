package fraymark.combat.engine;

import java.util.ArrayDeque;
import java.util.Deque;

/***
 * Contains a stack of all TurnPhases, to be resolved later.
 */
public class StateMachine {
    private final Deque<TurnPhase> stack = new ArrayDeque<>();

    public void pushPhase(TurnPhase phase) { stack.push(phase); }
    public void popPhase() { if (!stack.isEmpty()) stack.pop(); }
    public TurnPhase current() { return stack.peek(); }

    public void reset() {
        stack.clear();
        stack.push(TurnPhase.PLAYER_TURN);
    }
}