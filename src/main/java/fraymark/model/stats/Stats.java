package fraymark.model.stats;

/***
 * Stats is a dataclass storing the Stats of some Combatant. All setters and getters provided. <p>
 *
 * == STATS BREAKDOWN == <br>
 * maxHP: the maximum HP value. actual HP <= maxHP. <br>
 * atk: the "Attack," PHYSICAL attack value, corresponds to enemy's DEF. <br>
 * def: the "Defense," PHYSICAL defense value, corresponds to enemy's ATK. <br>
 * wil: the "Willpower," WEAVE attack value, corresponds to enemy's RES. <br>
 * res: the "Resilience," WEAVE defense value, corresponds to enemy's WIL. <br>
 * spd: the "Speed," the initiative value which determines turn order.<br>
 */
public class Stats {
    private int maxHP;
    private int atk;
    private int def;
    private int wil;
    private int res;
    private int spd;

    public Stats(int maxHP, int atk, int def, int wil, int res, int spd) {
        this.maxHP = maxHP;
        this.atk = atk;
        this.def = def;
        this.wil = wil;
        this.res = res;
        this.spd = spd;
    }

    public int getMaxHP() { return maxHP; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getWil() { return wil; }
    public int getRes() { return res; }
    public int getSpd() { return spd; }

    public void setAtk(int atk) { this.atk = atk ; }
    public void setDef(int def) { this.def = def; }
    public void setWil(int wil) { this.wil = wil ; }
    public void setRes(int res) { this.res = res; }
    public void setSpd(int spd) { this.spd = spd ; }

}
