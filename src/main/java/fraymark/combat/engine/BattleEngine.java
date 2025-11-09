package fraymark.combat.engine;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;
import fraymark.combat.events.*;
import fraymark.combat.damage.DamageContext;
import fraymark.combat.damage.pipeline.DamagePipeline;
import java.util.List;

/***
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

    public ActionResult performAction(BattleState state, Combatant actor,
                                      Action action, List<Combatant> targets) {
        ActionContext ctx = new ActionContext(actor, targets);
        ActionResult result = action.execute(ctx);

        for (CombatEvent ev : result.events()) {
            System.out.println("DEBUG: Event type=" + ev.getType() +
                    " message=" + ev.getMessage() +
                    " amount=" + ev.getAmount());
            if (ev.getType() == CombatEventType.DAMAGE) {
                DamageContext dctx = new DamageContext(
                        ev.getSource(), ev.getTarget(), ev.getAmount(), action, eventBus);
                pipeline.process(dctx); // ApplyDamageHandler updates HP
            } else {
                eventBus.publish(ev);   // other event types
            }
        }

        effects.onTurnEnd(state.getParty());
        effects.onTurnEnd(state.getEnemies());
        state.nextTurn();

        return result;
    }


    public void handleInterrupt(InterruptState interruptState, BattleState battle) {
        stateMachine.pushPhase(TurnPhase.INTERRUPT);
        performAction(battle, interruptState.getInterrupter(), interruptState.getAction(), interruptState.getTargets());
        stateMachine.popPhase();
    }
}