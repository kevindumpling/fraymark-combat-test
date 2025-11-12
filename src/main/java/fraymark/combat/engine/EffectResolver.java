// src/main/java/fraymark/combat/engine/EffectResolver.java
package fraymark.combat.engine;

import fraymark.combat.events.*;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;
import java.util.Iterator;
import java.util.List;

public class EffectResolver {
    private final EventBus bus;

    public EffectResolver(EventBus bus) {
        this.bus = bus;
    }

    public void onTurnStart(List<Combatant> all) {
        for (Combatant c : all) {
            int hpBefore = c.getResources().getHp();
            for (Effect e : c.getStatusEffects()) e.onTurnStart(c);
            int hpAfter = c.getResources().getHp();
            if (hpAfter < hpBefore) {
                bus.publish(CombatEvent.damageEvent(c, c, hpBefore - hpAfter, eName("start-of-turn")));
                for (Effect e: c.getStatusEffects()) {
                    bus.publish(CombatEvent.logEvent(c, c, c.getName() + " is affected by " + e.getName() + "!"));
                }
            }
        }
    }

    public void onTurnEnd(List<Combatant> all) {
        for (Combatant c : all) {
            Iterator<Effect> it = c.getStatusEffects().iterator();
            while (it.hasNext()) {
                Effect e = it.next();
                int hpBefore = c.getResources().getHp();
                e.onTurnEnd(c);
                int hpAfter = c.getResources().getHp();

                if (hpAfter < hpBefore) {
                    bus.publish(CombatEvent.damageEvent(c, c, hpBefore - hpAfter, e.getName()));
                    bus.publish(CombatEvent.logEvent(c, c, c.getName() + " is hurt by " + e.getName() + "."));
                }

                if (e.isExpired()) {
                    it.remove();
                    bus.publish(new CombatEvent(CombatEventType.STATUS_EXPIRED, c, c, 0, e.getName() + " expired on " +
                            c.getName() + "!"));
                }
            }
        }
    }

    private String eName(String s){ return s; }
}
