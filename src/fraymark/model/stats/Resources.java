package fraymark.model.stats;

/***
 * The Resources class contains all the information about a Combatant's resources, such as: <br>
 * - current HP <br>
 * - TRP (Threading Point) amount <br>
 * - MG (Momentum Gauge) amount <br>
 * - Instability amount <br>
 * - Barrier (damage protection) amount <br>
 */
public class Resources {
    private int hp;
    private int mg;   // Momentum
    private int trp;  // Threading Points
    private int instability;
    private int barrier;

    public Resources(int hp, int mg, int trp, int instability, int barrier) {
        this.hp = hp;
        this.mg = mg;
        this.trp = trp;
        this.instability = instability;
        this.barrier = barrier;
    }

    public int getHp() { return hp; }
    public int getMg() { return mg; }
    public int getTrp() { return trp; }
    public int getInstability() { return instability; }
    public int getBarrier() { return barrier; }

    public void setHp(int hp) { this.hp = hp; }
    public void setMg(int mg) { this.mg = mg; }
    public void setTrp(int trp) { this.trp = trp; }
    public void setInstability(int instability) { this.instability = instability; }
    public void setBarrier(int barrier) { this.barrier = barrier; }

    public void reduceBarrier(double amount) {
        barrier = Math.max(0, (int)(barrier - amount));
    }
}
