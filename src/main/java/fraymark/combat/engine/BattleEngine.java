package fraymark.combat.engine;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;
import fraymark.combat.events.*;
import fraymark.combat.damage.DamageContext;
import fraymark.combat.damage.pipeline.DamagePipeline;
import java.util.List;

/**
 * The main engine handling all battles. Performs actions, gets their results, handles interruptions.
 */
public class BattleEngine {
    private final EventBus eventBus;
    private final DamagePipeline pipeline;
    private final EffectResolver effects;
    private final StateMachine stateMachine = new StateMachine();

    public BattleEngine(EventBus bus, DamagePipeline pipeline, EffectResolver resolver) {
        this.eventBus = bus;
        this.pipeline = pipeline;
        this.effects = resolver;
        stateMachine.reset();
    }

    /**
     * Perform an action and process all its events.
     */
    public ActionResult performAction(BattleState state, Combatant actor,
                                      Action action, List<Combatant> targets) {
        if (actor == null) {
            System.err.println("performAction: actor is null — aborting.");
            return new ActionResult(List.of());
        }

        // Apply start-of-turn effects
        effects.onTurnStart(List.of(actor));

        // Check if actor can still act (might be stunned, dead, etc.)
        if (!canAct(actor)) {
            String msg = actor.getName() + " cannot act!";
            eventBus.publish(new CombatEvent(
                    CombatEventType.LOG, actor, actor, 0, msg
            ));

            // Still advance the turn
            effects.onTurnEnd(state.getParty());
            effects.onTurnEnd(state.getEnemies());
            state.nextTurn();

            return new ActionResult(List.of(
                    CombatEvent.logEvent(actor, actor, msg)
            ));
        }

        // Execute the action
        ActionContext ctx = new ActionContext(actor, targets);
        ActionResult result = action.execute(ctx);


        // Process all events from the action
        for (CombatEvent ev : result.events()) {
            eventBus.publish(ev);  // Publish the event.

            System.out.println("DEBUG: Event type=" + ev.getType() +
                    " message=" + ev.getMessage() +
                    " amount=" + ev.getAmount());

            if (ev.getType() == CombatEventType.DAMAGE) {
                DamageContext dctx = new DamageContext(
                        ev.getSource(), ev.getTarget(), ev.getAmount(), action, eventBus);
                pipeline.process(dctx); // ApplyDamageHandler updates HP
            } else {
                // TODO PASS
            }
        }

        // Apply end-of-turn effects
        effects.onTurnEnd(state.getParty());
        effects.onTurnEnd(state.getEnemies());

        // Advance to next turn
        state.nextTurn();

        // Check if battle is over
        if (state.isBattleOver()) {
            handleBattleEnd(state);
        }

        return result;
    }

    /**
     * Check if a combatant can act (not dead, not stunned, etc.)
     */
    private boolean canAct(Combatant actor) {
        // Dead check
        if (actor.getResources().getHp() <= 0) {
            return false;
        }

        // TODO: Check for stun/sleep/other disabling effects
        // for (Effect e : actor.getStatusEffects()) {
        //     if (e.preventsAction()) return false;
        // }

        return true;
    }

    /**
     * Handle battle end (victory or defeat).
     */
    private void handleBattleEnd(BattleState state) {
        BattleState.BattleResult result = state.getResult();
        String msg = switch (result) {
            case VICTORY -> "Victory! All enemies defeated!";
            case DEFEAT -> "Defeat! Your party has fallen!";
            case ONGOING -> ""; // Shouldn't happen
        };

        if (!msg.isEmpty()) {
            eventBus.publish(new CombatEvent(
                    CombatEventType.LOG, null, null, 0, msg
            ));
        }
    }

    /**
     * Handle an interrupt action.
     */
    public void handleInterrupt(InterruptState interruptState, BattleState battle) {
        stateMachine.pushPhase(TurnPhase.INTERRUPT);
        performAction(battle, interruptState.getInterrupter(),
                interruptState.getAction(), interruptState.getTargets());
        stateMachine.popPhase();
    }

    /**
     * Get the current turn phase from the state machine.
     */
    public TurnPhase getCurrentPhase() {
        return stateMachine.current();
    }
}