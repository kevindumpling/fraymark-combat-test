package fraymark.combat.damage;

import fraymark.model.combatants.Combatant;

/***
 * Specifies a Strategy to use when computing damage.
 */
public interface DamageCalculatorStrategy {
    double calculatePhysicalDamage(Combatant attacker, Combatant target, int power);
    double calculateWeaveDamage(Combatant attacker, Combatant target, int power, int trpSpent);
}