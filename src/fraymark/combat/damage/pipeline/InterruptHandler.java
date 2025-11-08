package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.events.CombatEvent;
import fraymark.combat.events.CombatEventType;
import fraymark.combat.events.EventBus;

/***
 * An InterruptHandler is a DamageHandler that can interrupt the currently resolving damage with an interruption.
 */
public class InterruptHandler implements DamageHandler {
    private final EventBus bus;

    public InterruptHandler(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public boolean handle(DamageContext ctx) {
        CombatEvent evt = new CombatEvent(
                CombatEventType.INTERRUPT_REQUEST,
                ctx.source,
                ctx.target,
                ctx.finalDamage,
                "Interrupt request before damage."
        );
        bus.publish(evt);

        if (evt.isCanceled()) {
            ctx.canceled = true;
            return true;
        }
        return false;
    }
}