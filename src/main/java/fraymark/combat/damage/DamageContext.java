package fraymark.combat.damage;

import fraymark.combat.events.EventBus;
import fraymark.model.actions.Action;
import fraymark.model.combatants.Combatant;

/***
 * Context passed through the damage pipeline.
 * Holds source, target, action, and bus references.
 */
public class DamageContext {
    private final Combatant source;
    private final Combatant target;
    private final Action action;
    private final EventBus bus;

    private double basePower;
    private double finalDamage;
    private boolean canceled;

    public DamageContext(Combatant source, Combatant target, double basePower, Action action, EventBus bus) {
        this.source = source;
        this.target = target;
        this.action = action;
        this.bus = bus;
        this.basePower = basePower;
        this.finalDamage = basePower;
        this.canceled = false;
    }

    public Combatant source() { return source; }
    public Combatant target() { return target; }
    public Action action() { return action; }
    public EventBus bus() { return bus; }

    public double basePower() { return basePower; }
    public void setBasePower(double basePower) { this.basePower = basePower; }

    public double finalDamage() { return finalDamage; }
    public void setFinalDamage(double finalDamage) { this.finalDamage = finalDamage; }

    public boolean canceled() { return canceled; }
    public void cancel() { this.canceled = true; }
}
