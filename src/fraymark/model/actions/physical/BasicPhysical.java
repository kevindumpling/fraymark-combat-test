package fraymark.model.actions.physical;

import fraymark.model.actions.*;
import fraymark.model.combatants.Combatant;
import java.util.List;
import fraymark.combat.events.CombatEvent;

/***
 * A placeholder for basic physical actions.
 * TODO: make better
 */
public class BasicPhysical implements Action {
    private final String name;
    private final int power;

    public BasicPhysical(String name, int power) {
        this.name = name;
        this.power = power;
    }

    @Override public String getName() { return name; }
    @Override public ActionType getType() { return ActionType.PHYSICAL_BASIC; }
    @Override public int getTrpCost() { return 0; }

    @Override
    public boolean canUse(Combatant user) { return true; }

    @Override
    public ActionResult execute(ActionContext ctx) {
        Combatant target = ctx.targets().getFirst();
        // placeholder logic
        CombatEvent e = new CombatEvent(null, ctx.user(), target, power, name);
        return new ActionResult(List.of(e));
    }
}