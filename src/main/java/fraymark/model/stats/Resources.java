package fraymark.model.stats;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
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
    private int defaultMgLoss = 10;  // The amount of MG that is lost by default on choosing a non-physical action.

    // stats for rolling HP
    private int pendingHpLoss = 0;                // queued damage not yet applied
    private double baseRollRatePerSecond = 10;  // default: 10 hp/sec
    private double rollRateMultiplier = 1.0;      // effects can change this
    private transient Timeline hpRoller;  // timeline for HP rolling that runs on UI thread
    private double rollCarry = 0.0;  // fractional accumulator for rolling HP

    public Resources(int hp, int mg, int trp, int barrier, int focus) {
        this.hp.set(hp);
        this.mg.set(mg);
        this.trp.set(trp);
        this.barrier.set(barrier);
        this.focus.set(focus);
    }

    // === rolling configuration ===
    public void setBaseRollRate(double perSec) { this.baseRollRatePerSecond = Math.max(0.0, perSec); }  // ← changed from 1.0
    public void setRollRateMultiplier(double m) { this.rollRateMultiplier = Math.max(0.0, m); }
    public double getEffectiveRollRate() { return baseRollRatePerSecond * rollRateMultiplier; }

    // === enqueue damage to roll ===
    public void enqueueRollingDamage(int amount) {
        if (amount <= 0) return;
        pendingHpLoss += amount;
        startHpRollerIfNeeded();
    }


    // === pause/resume hooks for future items
    public void pauseRolling() { if (hpRoller != null) hpRoller.pause(); }
    public void resumeRolling() { if (hpRoller != null) hpRoller.play(); }

    // === internal: UI animation ===
    private void startHpRollerIfNeeded() {
        if (hpRoller != null && hpRoller.getStatus() == Animation.Status.RUNNING) return;

        hpRoller = new Timeline(new KeyFrame(Duration.millis(16), e -> tick(0.016)));
        hpRoller.setCycleCount(Animation.INDEFINITE);
        hpRoller.play();
    }

    private void tick(double dtSeconds) {
        if (pendingHpLoss <= 0) {
            if (hpRoller != null) hpRoller.stop();
            return;
        }

        double rate = getEffectiveRollRate();          // HP per second
        double dec = rate * dtSeconds + rollCarry;     // accumulate fractional progress
        int step = (int) Math.floor(dec);              // whole HP we can actually apply now
        rollCarry = dec - step;                        // keep the remainder

        if (step <= 0) return;                         // nothing to apply this frame

        int apply = Math.min(step, pendingHpLoss);

        // apply once
        hp.set(Math.max(0, hp.get() - apply));
        pendingHpLoss -= apply;

        if (pendingHpLoss <= 0 && hpRoller != null) hpRoller.stop();
    }

    // === HP ===
    public int getHp() { return hp.get(); }
    public void setHp(int value) { hp.set(value); }
    public IntegerProperty hpProperty() { return hp; }
    public boolean isRolling() { return pendingHpLoss > 0; }

    // === MG ===
    public int getMg() { return mg.get(); }
    public void setMg(int value) { mg.set(value); }
    public IntegerProperty mgProperty() { return mg; }
    public int getDefaultMgLoss() { return this.defaultMgLoss; }
    public void setDefaultMgLoss(int loss) { this.defaultMgLoss = loss; }


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
