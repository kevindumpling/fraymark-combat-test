// fraymark/combat/engine/EffectResolver.java
package fraymark.combat.engine;

import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;
import fraymark.combat.events.*;
import java.util.*;

public class EffectResolver {
    private final EventBus bus;
    private final Map<Combatant, List<EffectInstance>> active = new HashMap<>();

    public EffectResolver(EventBus bus) { this.bus = bus; }

    public void apply(Combatant source, Combatant target, Effect e) {
        var list = active.computeIfAbsent(target, k -> new ArrayList<>());

        Optional<EffectInstance> sameKey = list.stream()
                .filter(inst -> inst.effect().key().equals(e.key()))
                .findFirst();

        if (sameKey.isPresent()) {
            var cur = sameKey.get();
            switch (e.stacking()) {
                case REFRESH_DURATION -> cur.setRemaining(Math.max(cur.remaining(), e.initialDuration()));
                case STACK_ADD -> {
                    // your accumulation policy — for now, refresh duration TODO
                    cur.setRemaining(Math.max(cur.remaining(), e.initialDuration()));
                }
                case REPLACE_IF_STRONGER -> {
                    cur.expire(target, bus, ExpireReason.OVERWRITTEN);
                    list.remove(cur);
                    var ni = new EffectInstance(e, source);
                    list.add(ni);
                    ni.apply(target);
                    refreshRollRate(target);
                    return;
                }
            }
            // Re-assert magnitude if desired on refresh
            refreshRollRate(target);
            return;
        }
        // brand new effect
        var inst = new EffectInstance(e, source);
        list.add(inst);
        inst.apply(target);   // applies magnitude once
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

    public List<CombatEvent> generateDotEvents(Collection<Combatant> targets) {
        List<CombatEvent> out = new ArrayList<>();

        for (var t : targets) {
            var list = active.get(t);
            if (list == null || list.isEmpty()) continue;

            // Per-turn hooks (non-damage behaviour)
            list.forEach(inst -> inst.end(t));

            // Periodic damage
            for (var inst : list) {
                var e   = inst.effect();
                var src = inst.source();    // you added this earlier
                int dmg = e.periodicDamage(src, t);
                if (dmg > 0) {
                    String msg = e.periodicDamageMessage(src, t, dmg);
                    out.add(new CombatEvent(
                            CombatEventType.DAMAGE,
                            src,
                            t,
                            dmg,
                            msg
                    ));
                }
            }
        }

        return out;
    }

    public void finalizeEndOfTurn(Collection<Combatant> targets) {
        for (var t : targets) {
            var list = active.get(t);
            if (list == null || list.isEmpty()) continue;

            var it = list.iterator();
            boolean changed = false;
            while (it.hasNext()) {
                var inst = it.next();
                if (inst.remaining() > 0) inst.decrement();
                if (inst.remaining() == 0) {
                    inst.expire(t, bus, ExpireReason.NATURAL); // logs fade here
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

    public List<EffectInstance> getInstances(Combatant t) {
        return active.getOrDefault(t, List.of());
    }

    public List<Effect> getEffects(Combatant t) {
        var list = active.getOrDefault(t, List.of());
        List<Effect> out = new ArrayList<>(list.size());
        for (var inst : list) out.add(inst.effect());
        return Collections.unmodifiableList(out);
    }

    public boolean hasEffectKey(Combatant t, String key) {
        var list = active.get(t);
        if (list == null) return false;
        for (var inst : list) {
            if (inst.effect().key().equals(key)) return true;
        }
        return false;
    }
}
