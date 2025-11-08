package fraymark.model.weapons;


import fraymark.model.actions.Action;
import java.util.List;

/***
 * A Weapon describes an equipment item that grants a new Action to its user.
 */
public class Weapon {
    private final String id;
    private final String name;
    private final WeaponType type;
    private final List<Action> grantedActions;

    public Weapon(String id, String name, WeaponType type, List<Action> grantedActions) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.grantedActions = grantedActions;
    }

    public String getName() { return name; }
    public WeaponType getType() { return type; }
    public List<Action> getGrantedActions() { return grantedActions; }
}
