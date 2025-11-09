package fraymark.model.weapons;

/***
 * WeaponType describes the sort of Weapon being used, primarily for measuring the user's Proficiency in that weapon.
 */
public enum WeaponType {
    RANGED,
    RANGED_FIREARM_LIGHT,
    RANGED_FIREARM_HEAVY,
    BLADED_LIGHT,
    HEAVY,
    SHIELD,
    BATON,
    TASER,
    NONE;

    public static WeaponType fromString(Object value) {
        if (value == null) return NONE;
        try {
            return WeaponType.valueOf(value.toString().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return NONE;
        }

    }
}
