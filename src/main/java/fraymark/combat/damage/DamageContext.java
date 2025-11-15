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

    // any potential TRP gain for this hit
    private int trpGain;

    private AttackRangeKind rangeKind = AttackRangeKind.MELEE;
    private boolean targetIsClose = false;
    private int baseMgGain = 0;
    private double mgGainMultiplier = 1.0;

    private boolean executed = false;

    private double defBypassPct = 0.0;
    private int defBypassFlat = 0;
    private double resBypassPct = 0.0;
    private int    resBypassFlat = 0;
    private double barrierIgnorePct = 0.0;

    private int trpSnapshot;
    private int trpToSpend;      // decided up-front for this action (once)
    private double trpScaleMul = 1.0;
    private boolean trpPlanned = false;
    private boolean trpDebited = false;
    private boolean primaryHit = true; // mark first target; others false

    private String customDamageLog; // optional override

    public DamageContext(Combatant source, Combatant target, double basePower, Action action, EventBus bus) {
        this.source = source;
        this.target = target;
        this.action = action;
        this.bus = bus;
        this.basePower = basePower;
        this.finalDamage = basePower;
        this.canceled = false;
        this.trpGain = 1;  // default
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


    public void withResBypassPct(double pct){ this.resBypassPct = pct; }
    public void withResBypassFlat(int flat){ this.resBypassFlat = flat; }
    public void withBarrierIgnorePct(double pct){ this.barrierIgnorePct = pct; }

    public double getResBypassPct(){ return resBypassPct; }
    public int getResBypassFlat(){ return resBypassFlat; }
    public double getBarrierIgnorePct(){ return barrierIgnorePct; }

    public DamageContext withTrpSnapshot(int t){ this.trpSnapshot=t; return this; }
    public int getTrpSnapshot(){ return trpSnapshot; }

    public void multiplyByTrp(double m){ this.trpScaleMul *= m; this.setFinalDamage(this.finalDamage()*m); }

    public DamageContext setTrpToSpend(int v){ this.trpToSpend = Math.max(0, v); this.trpPlanned = true; return this; }
    public int getTrpToSpend(){ return trpToSpend; }
    public boolean isTrpPlanned(){ return trpPlanned; }

    public DamageContext markTrpDebited(){ this.trpDebited = true; return this; }
    public boolean isTrpDebited(){ return trpDebited; }

    public DamageContext withPrimaryHit(boolean v){ this.primaryHit = v; return this; }
    public boolean isPrimaryHit(){ return primaryHit; }

    public DamageContext withCustomDamageLog(String msg) { this.customDamageLog = msg; return this; }
    public String getCustomDamageLog() { return customDamageLog; }


    public void setFinalTrpGain(int modified) { this.trpGain = modified; }

}

