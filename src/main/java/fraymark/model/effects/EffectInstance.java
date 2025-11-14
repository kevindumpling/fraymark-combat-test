// fraymark/model/effects/EffectInstance.java
package fraymark.model.effects;

import fraymark.combat.events.CombatEvent;
import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;

public class EffectInstance {
    private final Effect effect;
    private int remaining; // in turns; <=0 means indefinite
    private Combatant source;

    public EffectInstance(Effect effect, Combatant source) {
        this.effect = effect;
        this.remaining = effect.initialDuration();
        this.source = source;
    }

    public Effect effect() { return effect; }
    public Combatant source() { return source; }

    public int remaining() { return remaining; }
    public void setRemaining(int v) { remaining = v; }
    public void decrement() { if (remaining > 0) remaining--; }

    // Call the no-bus variants as per your Effect interface
    public void apply(Combatant tgt){ effect.onApply(tgt); }
    public void start(Combatant tgt){ effect.onTurnStart(tgt); }
    public void end(Combatant tgt){ effect.onTurnEnd(tgt); }

    // onExpire in your Effect takes a bus; keep passing it
    public void expire(Combatant tgt, EventBus bus, ExpireReason why){

        effect.onExpire(tgt, bus, why);

        String msg;
        switch (why) {
            case NATURAL ->
                    msg = effect.getName() + " on " + tgt.getName() + " fades.";
            case DISPELLED ->
                    msg = effect.getName() + " on " + tgt.getName() + " is dispelled.";
            case OVERWRITTEN ->
                    msg = effect.getName() + " on " + tgt.getName() + " is overwritten.";
            default ->
                    msg = effect.getName() + " on " + tgt.getName() + " ends.";
        }

        bus.publish(CombatEvent.logEvent(
                null,  // source (or null)
                tgt,
                msg
        ));
    }

}
