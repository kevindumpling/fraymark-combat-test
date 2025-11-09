package fraymark.model.combatants;

import fraymark.model.stats.Stats;
import fraymark.model.stats.Resources;
import fraymark.model.actions.Action;
import fraymark.model.effects.Effect;
import fraymark.model.weapons.Weapon;
import java.util.ArrayList;
import java.util.List;

/***
 * A Combatant is some entity capable of participating in combat.
 *
 */
public class Combatant {
    private final String id;
    private final String name;
    private Stats stats;
    private Resources resources;
    private Weapon weapon;
    private final List<Action> actions = new ArrayList<>();
    private final List<Effect> statusEffects = new ArrayList<>();
    private boolean armored;
    private boolean playerControlled;

    public Combatant(String id, String name, Stats stats, Resources resources, boolean armored, boolean playerControlled) {
        this.id = id;
        this.name = name;
        this.stats = stats;
        this.resources = resources;
        this.armored = armored;
        this.playerControlled = playerControlled;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Stats getStats() { return stats; }
    public Resources getResources() { return resources; }

    public List<Action> getActions() { return actions; }
    public List<Effect> getStatusEffects() { return statusEffects; }

    public void addAction(Action a) { actions.add(a); }
    public void addStatusEffect(Effect e) { statusEffects.add(e); }

    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }

    public boolean isArmored() { return armored; }
    public boolean isPlayerControlled() { return playerControlled; }

    public void addStatus(Effect effect) { this.statusEffects.add(effect); }

}
