// fraymark/model/effects/BarrierEffect.java
package fraymark.model.effects.impl;

import fraymark.combat.events.*;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;
import fraymark.model.effects.ExpireReason;
import fraymark.model.effects.StackingRule;

public class BarrierEffect implements Effect {
    private final double pct;   // 0..1
    private final int duration;
    private final String key;

    public BarrierEffect(double pct, int duration) {
        this.pct = Math.max(0, Math.min(1, pct));
        this.duration = Math.max(1, duration);
        this.key = "BARRIER:" + (int)(pct * 100);
    }

    @Override public int initialDuration() { return duration; }
    @Override public String key() { return key; }
    @Override public StackingRule stacking() { return StackingRule.REFRESH_DURATION; }
    @Override public String getName() { return "Barrier " + (int)(pct*100) + "%"; }

    @Override
    public void scaleMagnitude(double mul) {

    }

    @Override public void onApply(Combatant tgt) {
        var res = tgt.getResources();
        res.setBarrier(Math.min(1.0, res.getBarrier() + pct));
    }

    @Override
    public void onTurnStart(Combatant target) {

    }

    @Override
    public void onTurnEnd(Combatant target) {

    }

    @Override public void onExpire(Combatant tgt,
                                   fraymark.combat.events.EventBus bus,
                                   ExpireReason why) {
        var res = tgt.getResources();
        res.setBarrier(Math.max(0.0, res.getBarrier() - pct));
    }
}

