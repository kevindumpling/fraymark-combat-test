package fraymark.model.actions.physical;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;

import java.util.ArrayList;
import java.util.List;
import fraymark.combat.events.CombatEvent;
import fraymark.model.effects.Effect;
import fraymark.model.effects.EffectDescriptor;

/***
 * A placeholder for basic physical actions.
 * TODO: make better
 */
public class BasicPhysicalAction implements Physical {
    private final String name;
    private final int power;
    protected final List<EffectDescriptor> effectBundle = new ArrayList<>();
    private final String flavorOnUse;
    private final int trpGain;
    private final int mgGainOrCost;

    private final TargetingMode targeting;
    private final AttackRangeKind rangeKind;
    private final double aoeDamageMultiplier;               // default 1.0
    private final double aoeEffectMultiplier;      // default 1.0
    private final List<EffectDescriptor> aoeEffectBundle;   // default empty

    private final MomentumProfile momentumProfile;     // nullable
    private final CloseRangeProfile closeRangeProfile; // nullable
    private final ExecutionProfile executionProfile; // nullable

    public BasicPhysicalAction(String name, int power, int trpGain, int mgGainOrCost, MomentumProfile momentumProfile, CloseRangeProfile closeRangeProfile,
                               ExecutionProfile executionProfile, String flavorOnUse,
                               TargetingMode targeting, AttackRangeKind rangeKind,
                               double aoeDamageMultiplier, double aoeEffectMultiplier, List<EffectDescriptor> aoeEffectBundle) {
        this.name = name;
        this.power = power;
        this.flavorOnUse  = flavorOnUse;
        this.trpGain = trpGain;
        this.mgGainOrCost = mgGainOrCost;

        this.targeting = targeting;
        this.rangeKind =  rangeKind;
        this.aoeDamageMultiplier = aoeDamageMultiplier;
        this.aoeEffectMultiplier = aoeEffectMultiplier;
        this.aoeEffectBundle = aoeEffectBundle;

        this.momentumProfile = momentumProfile;
        this.closeRangeProfile = closeRangeProfile;
        this.executionProfile = executionProfile;
    }

    @Override public String getName() { return name; }
    @Override public ActionType getType() { return ActionType.PHYSICAL_BASIC; }
    @Override public int getTrpCost() { return 0; }

    @Override
    public boolean canUse(Combatant user) {
        if (this.mgGainOrCost >= 0){ return true; }
        return user.getResources().getMg() >= this.mgGainOrCost;
    }

    @Override
    public ActionResult execute(ActionContext context) {
        Combatant user = context.user();
        List<Combatant> targets = context.targets();
        List<CombatEvent> events = new ArrayList<>();

        // Give all benefits to the user.
        user.getResources().setTrp(user.getResources().getTrp() + this.trpGain);

        // Deal the effect.
        for (int i = 0; i < targets.size(); i++) {
            Combatant t = targets.get(i);
            boolean primary = (i == 0);

            // damage
            int base = this.power;
            int dmg = primary ? base : (int) Math.round(base * aoeDamageMultiplier);
            events.add(CombatEvent.damageEvent(user, t, dmg, "\n" + context.user().getName() + " used " + this.getName()));

            // effects: choose list, then (optionally) scale magnitudes for splash
            List<EffectDescriptor> list = primary
                    ? this.effectBundle
                    : (!aoeEffectBundle.isEmpty() ? aoeEffectBundle : this.effectBundle);

            for (EffectDescriptor desc : list) {
                Effect e = desc.instantiate(user, t);

                // Optional: scale magnitude for splash if using the main list
                if (!primary && aoeEffectBundle.isEmpty() && aoeEffectMultiplier != 1.0) {
                    e.scaleMagnitude(aoeEffectMultiplier); // implement a no-op if not supported
                }

                e.onApply(t);
                t.addStatus(e);
                events.add(CombatEvent.logEvent(user, t, t.getName() + " is affected by " + e.getName()));
            }
        }

        return new ActionResult(events);
    }

    public void addEffectDescriptor(EffectDescriptor d) { this.effectBundle.add(d); }

    @Override public String getFlavorOnUse() { return flavorOnUse; }
    @Override public TargetingMode getTargeting() { return targeting; }
    @Override public AttackRangeKind getRangeKind() { return rangeKind; }
    public double getAoeDamageMultiplier() { return aoeDamageMultiplier; }
    public double getAoeEffectMagnitudeMultiplier() { return aoeEffectMultiplier; }
    public List<EffectDescriptor> getAoeEffectBundle() { return aoeEffectBundle; }
    @Override public int getMgGainOrCost(){ return this.mgGainOrCost;}
    public int getMgGain() { return this.mgGainOrCost; }
    @Override public MomentumProfile getMomentumProfile() { return momentumProfile; }
    @Override public CloseRangeProfile getCloseRangeProfile() { return closeRangeProfile; }
    @Override public ExecutionProfile getExecutionProfile() { return executionProfile; }

}
