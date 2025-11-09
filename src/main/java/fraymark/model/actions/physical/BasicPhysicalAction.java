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
public class BasicPhysicalAction implements Action {
    private final String name;
    private final int power;
    protected final List<EffectDescriptor> effectBundle = new ArrayList<>();

    public BasicPhysicalAction(String name, int power) {
        this.name = name;
        this.power = power;
    }

    @Override public String getName() { return name; }
    @Override public ActionType getType() { return ActionType.PHYSICAL_BASIC; }
    @Override public int getTrpCost() { return 0; }

    @Override
    public boolean canUse(Combatant user) { return true; }

    @Override
    public ActionResult execute(ActionContext context) {
        Combatant user = context.user();
        Combatant target = context.targets().get(0);  // TODO: for now this just hits the first enemy
        List<CombatEvent> events = new ArrayList<>();

        events.add(CombatEvent.damageEvent(user, target, this.power, getName()));

        // Apply all attached effects.
        for (EffectDescriptor desc : effectBundle) {
            Effect effect = desc.instantiate(user, target);
            effect.onApply(target);
            target.addStatus(effect);
            events.add(CombatEvent.logEvent(
                    user, target, target.getName() + " affected by " + effect.getName()));
        }

        return new ActionResult(events);
    }
}