package fraymark.model.actions;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.EffectDescriptor;

import java.util.ArrayList;
import java.util.List;

/***
 * An Action is something that can be performed in combat.
 */
public interface Action {

    String getName();

    ActionType getType();

    int getTrpCost();

    ActionResult execute(ActionContext context);

    boolean canUse(Combatant user);

    default TargetingMode getTargeting() { return TargetingMode.SINGLE; }
    default AttackRangeKind getRangeKind() { return AttackRangeKind.ALL; }
    default double getAoeDamageMultiplier() { return 1.0; }
    default double getAoeEffectMagnitudeMultiplier() { return 1.0; }
    default List<EffectDescriptor> getAoeEffectBundle() { return new ArrayList<EffectDescriptor>(); }
}