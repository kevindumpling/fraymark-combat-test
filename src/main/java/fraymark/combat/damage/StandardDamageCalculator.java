package fraymark.combat.damage;

import fraymark.model.combatants.Combatant;
import fraymark.model.stats.Stats;
import fraymark.model.stats.Resources;

/***
 * The standard Strategy for calculating damage, using the default formulas.
 */
public class StandardDamageCalculator implements DamageCalculatorStrategy {

    @Override
    public double calculatePhysicalDamage(Combatant attacker, Combatant target, int power) {
        Stats a = attacker.getStats();
        Stats t = target.getStats();
        Resources r = attacker.getResources();

        int atk = a.getAtk();
        int defEff = t.getDef() + (target.isArmored() ? 10 : 0);
        double ratio = atk / (double) (atk + defEff);

        double mgBonus = 1.0 + (r.getMg() / 100.0 * 0.25);
        return power * ratio * mgBonus;
    }

    @Override
    public double calculateWeaveDamage(Combatant attacker, Combatant target, int power, int trpSpent) {
        Stats a = attacker.getStats();
        Stats t = target.getStats();

        int wil = a.getWil();
        int res = t.getRes();
        double ratio = wil / (double) (wil + res);
        double trpBonus = 1.0 + 0.1 * trpSpent;

        return power * ratio * trpBonus;
    }
}