package fraymark.model.stats;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/***
 * The Resources class contains all the information about a Combatant's resources, such as: <br>
 * - current HP <br>
 * - TRP (Threading Point) amount <br>
 * - MG (Momentum Gauge) amount <br>
 * - Instability amount <br>
 * - Barrier (damage protection) amount <br>
 */
public class Resources {
    private final IntegerProperty hp = new SimpleIntegerProperty();
    private final IntegerProperty mg = new SimpleIntegerProperty();
    private final IntegerProperty trp = new SimpleIntegerProperty();
    private final IntegerProperty barrier = new SimpleIntegerProperty();
    private final IntegerProperty focus = new SimpleIntegerProperty();

    public Resources(int hp, int mg, int trp, int barrier, int focus) {
        this.hp.set(hp);
        this.mg.set(mg);
        this.trp.set(trp);
        this.barrier.set(barrier);
        this.focus.set(focus);
    }

    // === HP ===
    public int getHp() { return hp.get(); }
    public void setHp(int value) { hp.set(value); }
    public IntegerProperty hpProperty() { return hp; }

    // === MG ===
    public int getMg() { return mg.get(); }
    public void setMg(int value) { mg.set(value); }
    public IntegerProperty mgProperty() { return mg; }

    // === TRP ===
    public int getTrp() { return trp.get(); }
    public void setTrp(int value) { trp.set(value); }
    public IntegerProperty trpProperty() { return trp; }

    // === Barrier ===
    public int getBarrier() { return barrier.get(); }
    public void setBarrier(int value) { barrier.set(value); }

    public IntegerProperty barrierProperty() { return barrier; }

    // === Focus ===
    public int getFocus() { return focus.get(); }
    public void setFocus(int value) { focus.set(value); }
    public IntegerProperty focusProperty() { return focus; }
}
