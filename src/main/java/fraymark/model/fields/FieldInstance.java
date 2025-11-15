package fraymark.model.fields;

import fraymark.combat.engine.BattleState;
import fraymark.combat.events.CombatEvent;
import fraymark.combat.events.EventBus;
import fraymark.model.effects.ExpireReason;

public class FieldInstance {
    private final Field field;
    private int remaining;         // turns left; 0 or <0 = infinite
    private boolean applied = false;
    private boolean infinite;

    public FieldInstance(Field field) {
        this.field = field;
        this.remaining = field.initialDuration();
        this.infinite  = (remaining <= 0); // zero or negative = infinite
    }

    public Field field() { return field; }
    public int remaining() { return remaining; }

    public void applyIfNeeded(BattleState state) {
        if (!applied) {
            applied = true;
            field.onApply(state);
        }
    }

    public void tickRound(BattleState state) {
        field.onRoundTick(state);
        if (remaining > 0) remaining--;
    }

    public void expire(BattleState state, EventBus bus, ExpireReason reason) {
        field.onExpire(state, reason);
        String msg = switch (reason) {
            case NATURAL    -> field.getName() + " fades from the battlefield.";
            case DISPELLED  -> field.getName() + " is dispelled.";
            case OVERWRITTEN-> field.getName() + " is overwritten.";
        };
        bus.publish(CombatEvent.logEvent(null, null, msg));
    }

    public boolean isInfinite() { return this.infinite; }
}
