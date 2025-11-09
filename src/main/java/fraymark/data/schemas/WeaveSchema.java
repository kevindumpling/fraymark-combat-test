package fraymark.data.schemas;

import java.util.List;

/***
 * Defines the contents of a Weave JSON file.
 */
public class WeaveSchema {
    public String id;
    public String name;
    public int power;
    public int trpCost;
    public List<String> effects;
    public String description;
}