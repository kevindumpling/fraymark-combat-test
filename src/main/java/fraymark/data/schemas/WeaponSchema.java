package fraymark.data.schemas;

import java.util.List;
/***
 * Defines the contents of a Weapon JSON file.
 */
public class WeaponSchema {
    public String id;
    public String name;
    public String type;
    public List<String> actions; // refers to Action IDs
}