package fraymark.combat.events;

import fraymark.model.combatants.Combatant;

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

    public CombatEvent(CombatEventType type, Combatant source, Combatant target, double amount, String message) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.amount = amount;
        this.message = message;
    }

    public CombatEventType getType() { return type; }
    public Combatant getSource() { return source; }
    public Combatant getTarget() { return target; }
    public double getAmount() { return amount; }
    public String getMessage() { return message; }
    public boolean isCanceled() { return canceled; }
    public void cancel() { this.canceled = true; }

    public static CombatEvent damageEvent(Combatant s, Combatant t, double dmg, String label) {
        return new CombatEvent(CombatEventType.DAMAGE, s, t, dmg, s.getName() + " uses " + label + " on " + t.getName());
    }

    public static CombatEvent logEvent(Combatant s, Combatant t, String msg) {
        return new CombatEvent(CombatEventType.LOG, s, t, 0, msg);
    }
}