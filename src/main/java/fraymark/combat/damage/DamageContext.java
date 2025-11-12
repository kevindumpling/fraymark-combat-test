package fraymark.combat.damage;

import fraymark.combat.events.EventBus;
import fraymark.model.actions.Action;
import fraymark.model.actions.AttackRangeKind;
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

    private AttackRangeKind rangeKind = AttackRangeKind.MELEE;
    private boolean targetIsClose = false;
    private int baseMgGain = 0;
    private double mgGainMultiplier = 1.0;
    private double defBypassPct = 0.0;
    private int defBypassFlat = 0;
    private boolean executed = false;

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

    public DamageContext withRangeKind(AttackRangeKind k){ this.rangeKind=k; return this; }
    public DamageContext withTargetIsClose(boolean b){ this.targetIsClose=b; return this; }
    public DamageContext withBaseMgGain(int g){ this.baseMgGain=g; return this; }
    public void multiplyFinalDamage(double mul){ this.finalDamage *= mul; }
    public void addDefenseBypassPct(double pct){ this.defBypassPct += pct; }
    public void multiplyMgGain(double mul){ this.mgGainMultiplier *= mul; }

    public AttackRangeKind getRangeKind(){ return this.rangeKind; }
    public boolean getTargetIsClose(){ return this.targetIsClose; }


    public int getBaseMgGain() {
        return this.baseMgGain;
    }

    public double getMgGainMultiplier() {
        return this.mgGainMultiplier;
    }

    public double getDefBypassPct() { return defBypassPct; }
    public int getDefBypassFlat() { return defBypassFlat; }

    public void markExecuted() { this.executed = true; this.cancel(); }
    public boolean wasExecuted() { return executed; }
}
