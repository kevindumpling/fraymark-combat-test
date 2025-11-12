package fraymark.model.actions.weaves;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;
import fraymark.combat.events.CombatEvent;
import fraymark.model.stats.Resources;

import java.util.*;

import static java.lang.Math.max;

/***
 * A WeaveAction represents a Weave attack.
 */
public class WeaveAction implements Action {
    protected final List<EffectDescriptor> effectBundle = new ArrayList<>();
    protected final int power;
    protected final int trpCost;
    protected final String name;
    private final String flavorOnUse;

    private final TargetingMode targeting;
    private final AttackRangeKind rangeKind;
    private final double aoeDamageMultiplier;               // default 1.0
    private final double aoeEffectMultiplier;      // default 1.0
    private final List<EffectDescriptor> aoeEffectBundle;   // default empty

    public WeaveAction(String name, int power, int trpCost, String flavorOnUse, TargetingMode targeting, AttackRangeKind rangeKind,
                       double aoeDamageMultiplier, double aoeEffectMultiplier, List<EffectDescriptor> aoeEffectBundle) {
        this.power = power;
        this.name = name;
        this.trpCost = trpCost;
        this.flavorOnUse = flavorOnUse;

        this.targeting = targeting;
        this.rangeKind = rangeKind;
        this.aoeDamageMultiplier = aoeDamageMultiplier;
        this.aoeEffectMultiplier = aoeEffectMultiplier;
        this.aoeEffectBundle = aoeEffectBundle;
    }

    @Override
    public ActionResult execute(ActionContext context) {
        List<Combatant> targets = context.targets();
        List<CombatEvent> events = new ArrayList<>();

        // All weave actions will lower the user's MG, since they cannot have
        // chosen both a physical and weave action at once.
        Resources userResources = context.user().getResources();
        userResources.setMg(max(0, userResources.getMg() - userResources.getDefaultMgLoss()));

        // Apply the effects to the targets.
        for (Combatant c : targets){
            Combatant user = context.user();
            Combatant target = c;

            events.add(CombatEvent.damageEvent(user, target, this.power, "\n" + user.getName() + " tried " + getName()));
            user.getResources().setTrp(user.getResources().getTrp() - this.trpCost);

            // Add on-use flavor line (if provided)
            if (flavorOnUse != null && !flavorOnUse.isBlank()) {
                events.add(CombatEvent.logEvent(
                        user, target, flavorOnUse
                ));
            }
            // Apply all attached effects.
            for (EffectDescriptor desc : effectBundle) {
                Effect effect = desc.instantiate(user, target);
                effect.onApply(target);
                target.addStatus(effect);
                events.add(CombatEvent.logEvent(
                        user, target, target.getName() + " was affected by " + effect.getName() + "!"));
            }


        }


        return new ActionResult(events);
    }

    @Override
    public boolean canUse(Combatant user) {
        return user.getResources().getTrp() >= trpCost;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ActionType getType() {
        return ActionType.WEAVE;
    }

    @Override
    public int getTrpCost() { return trpCost; }

    public void addEffectDescriptor(EffectDescriptor d) { this.effectBundle.add(d); }

    public String getFlavorOnUse() { return flavorOnUse; }

    @Override public TargetingMode getTargeting() { return targeting; }
    @Override public AttackRangeKind getRangeKind() { return rangeKind; }
    public double getAoeDamageMultiplier() { return aoeDamageMultiplier; }
    public double getAoeEffectMagnitudeMultiplier() { return aoeEffectMultiplier; }
    public List<EffectDescriptor> getAoeEffectBundle() { return aoeEffectBundle; }
}