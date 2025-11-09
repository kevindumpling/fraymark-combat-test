package fraymark.model.effects;

import fraymark.model.combatants.Combatant;

/***
 * A SimpleEffect has one effect. TODO!
 */
public class SimpleEffect implements Effect {
    public SimpleEffect(String name, int magnitude, int duration) {
    }

    @Override
    public void onApply(Combatant target) {

    }

    @Override
    public void onTurnStart(Combatant target) {

    }

    @Override
    public void onTurnEnd(Combatant target) {

    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }
}
