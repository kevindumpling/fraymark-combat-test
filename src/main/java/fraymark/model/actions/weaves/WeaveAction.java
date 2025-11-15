package fraymark.model.actions.weaves;

import fraymark.combat.damage.pipeline.TrpScalingHandler;
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
public class WeaveAction implements Weave {
    protected final int power;
    protected final int trpCost;
    protected final String name;
    private final String flavorOnUse;
    protected final WeaveSchool school;
    protected final List<EffectDescriptor> effectBundle = new ArrayList<>();
    protected final List<String> fieldIds = new ArrayList<>();

    private final TargetingMode targeting;
    private final AttackRangeKind rangeKind;
    private final double aoeDamageMultiplier;               // default 1.0
    private final double aoeEffectMultiplier;      // default 1.0
    private final List<EffectDescriptor> aoeEffectBundle;   // default empty

    private final double barrierIgnorePct;  // default 0
    private final double resBypassPct;      // default 0
    private final int    resBypassFlat;     // default 0

    private final int trpBaseCost;                         // base TRP cost the handler will read
    private final TrpSpendMode trpSpendMode;               // FLAT / VARIABLE / ALL_REMAINING, etc.
    private final TrpScalingProfile trpScalingProfile;         // nullable if not using scaling

    public WeaveAction(String name, WeaveSchool school, int power, int trpCost, String flavorOnUse, double barrierIgnorePct,
                       double resBypassPct, int resBypassFlat, TrpSpendMode trpSpendMode, TrpScalingProfile trpScalingProfile,
                       TargetingMode targeting, AttackRangeKind rangeKind,
                       double aoeDamageMultiplier, double aoeEffectMultiplier, List<EffectDescriptor> aoeEffectBundle) {
        this.power = power;
        this.school = school;
        this.name = name;
        this.trpCost = trpCost;
        this.flavorOnUse = flavorOnUse;
        this.barrierIgnorePct =  barrierIgnorePct;
        this.resBypassPct = resBypassPct;
        this.resBypassFlat = resBypassFlat;
        this.trpBaseCost = trpCost;             // handler reads this
        this.trpSpendMode = (trpSpendMode != null) ? trpSpendMode : TrpSpendMode.FLAT;
        this.trpScalingProfile = trpScalingProfile;

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

            // Add on-use flavor line (if provided)
            if (flavorOnUse != null && !flavorOnUse.isBlank()) {
                events.add(CombatEvent.logEvent(
                        user, target, flavorOnUse
                ));
            }
            // Apply all attached effects.
            for (EffectDescriptor desc : effectBundle) {
                Effect effect = desc.instantiate(user, target);
                events.add(CombatEvent.applyEffect(user, target, effect,
                        target.getName() + " was affected by " + effect.getName() + "!"));
            }
            // Apply all fields.
            if (!fieldIds.isEmpty()) {
                for (String id : fieldIds) {
                    String msg = user.getName() + " altered the field: " + id + "!";
                    events.add(CombatEvent.addFieldEvent(user, id, msg));
                }
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

    @Override
    public String getFlavorOnUse() { return flavorOnUse; }

    @Override public TargetingMode getTargeting() { return targeting; }
    @Override public AttackRangeKind getRangeKind() { return rangeKind; }
    public double getAoeDamageMultiplier() { return aoeDamageMultiplier; }
    public double getAoeEffectMagnitudeMultiplier() { return aoeEffectMultiplier; }
    public List<EffectDescriptor> getAoeEffectBundle() { return aoeEffectBundle; }
    @Override public double getResBypassPct() { return resBypassPct; }
    @Override public int getResBypassFlat() { return resBypassFlat; }



    @Override public double getBarrierIgnorePct() { return barrierIgnorePct; }
    @Override public WeaveSchool getSchool() { return school; }
    @Override public int getTrpBaseCost() { return trpBaseCost; }
    @Override public TrpSpendMode getTrpSpendMode() { return trpSpendMode; }
    @Override public TrpScalingProfile getTrpScalingProfile() { return trpScalingProfile; }
    public void addFieldId(String id){ this.fieldIds.add(id); }
}