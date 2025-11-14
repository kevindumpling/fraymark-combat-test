package fraymark.combat.engine;

import fraymark.model.actions.*;
import fraymark.model.actions.physical.Physical;
import fraymark.model.actions.weaves.Weave;
import fraymark.model.actions.weaves.WeaveAction;
import fraymark.model.combatants.Combatant;
import fraymark.combat.events.*;
import fraymark.combat.damage.DamageContext;
import fraymark.combat.damage.pipeline.DamagePipeline;
import fraymark.model.position.DistanceTier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * The main engine handling all battles. Performs actions, gets their results, handles interruptions.
 */
public class BattleEngine {
    private final EventBus eventBus;
    private final DamagePipeline pipeline;
    private final EffectResolver effects;
    private final StateMachine stateMachine = new StateMachine();
    private final Map<String, Integer> trpOverrides = new HashMap<>();

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
            resolveEndOfTurn(state);
            state.nextTurn();

            return new ActionResult(List.of(
                    CombatEvent.logEvent(actor, actor, msg)
            ));
        }

        // === Execute the action ===

        // Pre-announce
        Combatant primary = targets.isEmpty() ? actor : targets.get(0);
        if (action instanceof WeaveAction wa) {
            eventBus.publish(CombatEvent.logEvent(actor, primary,
                    "\n"+actor.getName() + " tried " + wa.getName() + "!"));
            if (wa.getFlavorOnUse() != null && !wa.getFlavorOnUse().isBlank())
                eventBus.publish(CombatEvent.logEvent(actor, primary, wa.getFlavorOnUse()));
        } else if (action instanceof Physical pa) {
            eventBus.publish(CombatEvent.logEvent(actor, primary,
                  "\n"+  actor.getName() + " used " + pa.getName() + "!"));
            if (pa.getFlavorOnUse() != null && !pa.getFlavorOnUse().isBlank())
                eventBus.publish(CombatEvent.logEvent(actor, primary, pa.getFlavorOnUse()));
        }
        ActionContext ctx = new ActionContext(actor, targets);
        ActionResult result = action.execute(ctx);
        final int trpSnap = actor.getResources().getTrp();

        int damageSeen = 0;

        // Process all events from the action
        for (CombatEvent ev : result.events()) {

            System.out.println("DEBUG: Event type=" + ev.getType() +
                    " message=" + ev.getMessage() +
                    " amount=" + ev.getAmount());

            if (ev.getType() != CombatEventType.DAMAGE) {
                switch (ev.getType()) {
                    case EFFECT_APPLIED -> {
                        eventBus.publish(ev); // log first
                        effects.apply(ev.getSource(), ev.getTarget(), ev.getEffect()); // actually apply
                    }
                    default -> eventBus.publish(ev);
                }

            } else {
                boolean isPrimary = (damageSeen++ == 0);

                boolean targetIsAlly = state.getParty().contains(ev.getTarget());
                var lineup = targetIsAlly ? state.getPartyLineup() : state.getEnemyLineup();
                boolean isClose = lineup.tierOf(ev.getTarget()) == DistanceTier.CLOSE;

                int baseMg = (action instanceof Physical pa) ? pa.getMgGainOrCost() : 0;

                DamageContext dctx = new DamageContext(
                        ev.getSource(), ev.getTarget(), ev.getAmount(), action, eventBus)
                        .withRangeKind(action.getRangeKind())
                        .withTargetIsClose(isClose)
                        .withBaseMgGain(baseMg);
                if (action instanceof Weave wa) {
                    dctx.withBarrierIgnorePct(wa.getBarrierIgnorePct());
                    dctx.withResBypassPct(wa.getResBypassPct());
                    dctx.withResBypassFlat(wa.getResBypassFlat());
                    dctx.withTrpSnapshot(trpSnap);

                    // If UI chose TRP spend, lock it in here.
                    Integer override = getTrpOverride(actor, wa);
                    if (override != null && override > 0) {
                        dctx.setTrpToSpend(override);
                    }

                    dctx.withPrimaryHit(isPrimary);

                }
                pipeline.process(dctx); // ApplyDamageHandler updates HP
            }
        }

        resolveEndOfTurn(state);

        // Advance to next turn
        state.nextTurn();

        // Check if battle is over
        if (state.isBattleOver()) {
            handleBattleEnd(state);
        }

        return result;
    }

    private void resolveEndOfTurn(BattleState state){
        // === End-of-turn phase 1: generate DoT damage events ===
        List<CombatEvent> dotEvents = new ArrayList<>();
        dotEvents.addAll(effects.generateDotEvents(state.getParty()));
        dotEvents.addAll(effects.generateDotEvents(state.getEnemies()));

        for (CombatEvent ev : dotEvents) {
            System.out.println("DEBUG: DOT Event type=" + ev.getType() +
                    " message=" + ev.getMessage() +
                    " amount=" + ev.getAmount());

            if (ev.getType() != CombatEventType.DAMAGE) {
                eventBus.publish(ev);
                continue;
            }

            boolean targetIsAlly = state.getParty().contains(ev.getTarget());
            var lineup = targetIsAlly ? state.getPartyLineup() : state.getEnemyLineup();
            boolean isClose = lineup.tierOf(ev.getTarget()) == DistanceTier.CLOSE;

            DamageContext dctx = new DamageContext(
                    ev.getSource(),
                    ev.getTarget(),
                    ev.getAmount(),
                    null,          // no originating Action
                    eventBus)
                    .withRangeKind(AttackRangeKind.ALL)
                    .withTargetIsClose(isClose)
                    .withBaseMgGain(0)
                    .withCustomDamageLog(ev.getMessage()); // use the DOT-specific message

            // Optionally, if you want handlers to know this is DoT, add:
            // dctx.setSourceEffect(ev.getEffect()); // if you stored it

            pipeline.process(dctx);
        }

        // === End-of-turn phase 2: decrement duration & expire (fade logs) ===
        effects.finalizeEndOfTurn(state.getParty());
        effects.finalizeEndOfTurn(state.getEnemies());
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

    private String trpKey(Combatant actor, Weave weave) {
        return actor.getId() + "|" + weave.getName();
    }

    /** Called by UI before performAction for VARIABLE weaves. */
    public void setTrpOverride(Combatant actor, Weave weave, int totalSpend) {
        if (actor == null || weave == null) return;
        trpOverrides.put(trpKey(actor, weave), Math.max(0, totalSpend));
    }

    /** Read (but do not remove) the override for this cast, if any. */
    private Integer getTrpOverride(Combatant actor, Weave weave) {
        return trpOverrides.get(trpKey(actor, weave));
    }
}