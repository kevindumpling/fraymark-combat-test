package fraymark.data.schemas;

import java.util.List;
/***
 * Defines the contents of a Character JSON file.
 */
public class CharacterSchema {
    public String id;
    public String name;
    public int maxHP;
    public int atk;
    public int def;
    public int wil;
    public int res;
    public int spd;
    public boolean armored;
    public List<String> actions; // refers to weave or physical IDs
    public String weapon; // references Weapon ID
}