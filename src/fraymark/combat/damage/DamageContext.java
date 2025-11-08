package fraymark.combat.damage;

import fraymark.model.combatants.Combatant;

/***
 * Contains contextual information about damage, such as its source and target.
 */
public class DamageContext {
    public final Combatant source;
    public final Combatant target;
    public double basePower;
    public double finalDamage;
    public boolean canceled;

    public DamageContext(Combatant source, Combatant target, double basePower) {
        this.source = source;
        this.target = target;
        this.basePower = basePower;
        this.finalDamage = basePower;
        this.canceled = false;
    }
}