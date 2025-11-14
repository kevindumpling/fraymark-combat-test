package fraymark.combat.events;

import fraymark.model.combatants.Combatant;
import fraymark.model.effects.Effect;

/***
 * A CombatEvent specifies something that happens during Combat, which must be some CombatEventType.
 */
public class CombatEvent {
    private final CombatEventType type;
    private final Combatant source;
    private final Combatant target;
    private final double amount;
    private final String message;
    private boolean canceled = false;
    private Effect effect; // nullable

    public CombatEvent(CombatEventType type, Combatant source, Combatant target, double amount, String message) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.amount = amount;
        this.message = message;
    }


    public CombatEvent(CombatEventType type, Combatant source, Combatant target, double amount, String message, Effect effect) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.amount = amount;
        this.message = message;
        this.effect = effect;
    }

    public CombatEventType getType() { return type; }
    public Combatant getSource() { return source; }
    public Combatant getTarget() { return target; }
    public double getAmount() { return amount; }
    public String getMessage() { return message; }
    public boolean isCanceled() { return canceled; }
    public void cancel() { this.canceled = true; }

    public static CombatEvent damageEvent(Combatant s, Combatant t, double dmg, String label) {
        return new CombatEvent(CombatEventType.DAMAGE, s, t, dmg, label + " on " + t.getName() + "!");
    }

    public static CombatEvent logEvent(Combatant s, Combatant t, String msg) {
        return new CombatEvent(CombatEventType.LOG, s, t, 0, msg);
    }

    public static CombatEvent applyEffect(Combatant src, Combatant tgt, Effect e, String msg){
        return new CombatEvent(CombatEventType.EFFECT_APPLIED, src, tgt, 0, msg, e);
    }

    public Effect getEffect() {
        if (this.effect == null){ return null;}

        return this.effect;
    }
}