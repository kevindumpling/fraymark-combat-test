package fraymark.model.actions.weaves;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.*;
import fraymark.combat.events.CombatEvent;

import java.util.*;

/***
 * A WeaveAction represents a Weave attack.
 */
public class WeaveAction implements Action {
    protected final List<EffectDescriptor> effectBundle = new ArrayList<>();
    protected final int power;
    protected final int trpCost;
    protected final String name;

    public WeaveAction(String name, int power, int trpCost) {
        this.power = power;
        this.name = name;
        this.trpCost = trpCost;
    }

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
}