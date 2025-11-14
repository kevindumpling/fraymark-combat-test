// fraymark/combat/engine/EffectResolver.java
package fraymark.combat.engine;

import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;

import java.util.*;

public class EffectResolver {
    private final EventBus bus;
    private final Map<Combatant, List<EffectInstance>> active = new HashMap<>();

    public EffectResolver(EventBus bus) { this.bus = bus; }

    public void apply(Combatant target, Effect e) {
        var list = active.computeIfAbsent(target, k -> new ArrayList<>());

        Optional<EffectInstance> sameKey = list.stream()
                .filter(inst -> inst.effect().key().equals(e.key()))
                .findFirst();

        if (sameKey.isPresent()) {
            var cur = sameKey.get();
            switch (e.stacking()) {
                case REFRESH_DURATION -> cur.setRemaining(Math.max(cur.remaining(), e.initialDuration()));
                case STACK_ADD -> {
                    // your accumulation policy — for now, refresh duration
                    cur.setRemaining(Math.max(cur.remaining(), e.initialDuration()));
                }
                case REPLACE_IF_STRONGER -> {
                    cur.expire(target, bus, ExpireReason.OVERWRITTEN);
                    list.remove(cur);
                    var ni = new EffectInstance(e);
                    list.add(ni);
                    ni.apply(target);
                    refreshRollRate(target);
                    return;
                }
            }
            // Re-assert magnitude if desired on refresh
            cur.effect().onApply(target);
            refreshRollRate(target);
            return;
        }

        // brand new
        var inst = new EffectInstance(e);
        list.add(inst);
        inst.apply(target);
        refreshRollRate(target);
    }

    public void onTurnStart(Collection<Combatant> targets) {
        for (var t : targets) {
            var list = active.get(t);
            if (list == null) continue;
            list.forEach(inst -> inst.start(t));
            // roll-rate usually unchanged at start, so no refresh required here
        }
    }

    public void onTurnEnd(Collection<Combatant> targets) {
        for (var t : targets) {
            var list = active.get(t);
            if (list == null) continue;

            // 1) per-turn hooks
            list.forEach(inst -> inst.end(t));

            // 2) decrement & expire
            var it = list.iterator();
            boolean changed = false;
            while (it.hasNext()) {
                var inst = it.next();
                if (inst.remaining() > 0) inst.decrement();
                if (inst.remaining() == 0) {
                    inst.expire(t, bus, ExpireReason.NATURAL);
                    it.remove();
                    changed = true;
                }
            }
            if (changed) refreshRollRate(t);
        }
    }

    private void refreshRollRate(Combatant t) {
        double mul = 1.0;
        var list = active.getOrDefault(t, List.of());
        for (var inst : list) {
            mul *= inst.effect().rollRateMultiplier(); // multiplicative stacking
        }
        t.getResources().setRollRateMultiplier(mul);
    }

    public void dispel(Combatant t, String key) {
        var list = active.get(t);
        if (list == null || list.isEmpty()) return;

        boolean changed = false;
        var it = list.iterator();
        while (it.hasNext()) {
            var inst = it.next();
            if (inst.effect().key().equals(key)) {
                inst.expire(t, bus, ExpireReason.DISPELLED);
                it.remove();
                changed = true;
            }
        }
        if (changed) refreshRollRate(t);
    }
}
