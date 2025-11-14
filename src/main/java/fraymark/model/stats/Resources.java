package fraymark.model.stats;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/***
 * The Resources class contains all the information about a Combatant's resources, such as: <br>
 * - current HP <br>
 * - TRP (Threading Point) amount, from 0-any int <br>
 * - MG (Momentum Gauge) amount, from 0-100 <br>
 * - Instability amount from 0-100 <br>
 * - Barrier (damage protection) amount as a percent from 0.0 to 0.1 <br>
 */
public class Resources {
    private final IntegerProperty hp = new SimpleIntegerProperty();
    private final IntegerProperty mg = new SimpleIntegerProperty();
    private final IntegerProperty trp = new SimpleIntegerProperty();
    private final DoubleProperty barrier = new SimpleDoubleProperty();
    private final IntegerProperty focus = new SimpleIntegerProperty();
    private final IntegerProperty armorAmount = new SimpleIntegerProperty();

    private int defaultMgLoss = 10;  // The amount of MG that is lost by default on choosing a non-physical action.

    // stats for rolling HP
    private int pendingHpLoss = 0;                // queued damage not yet applied
    private double baseRollRatePerSecond = 10;  // default: 10 hp/sec
    private double rollRateMultiplier = 1.0;      // effects can change this
    private transient Timeline hpRoller;  // timeline for HP rolling that runs on UI thread
    private double rollCarry = 0.0;  // fractional accumulator for rolling HP

    public Resources(int hp, int mg, int trp, double barrier, int armorAmount) {
        this.hp.set(hp);
        this.mg.set(mg);
        this.trp.set(trp);
        this.barrier.set(barrier);
        this.armorAmount.set(armorAmount);
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

    public void forceKill() {
        // stop rolling and set HP to 0 immediately
        pendingHpLoss = 0;
        if (hpRoller != null) hpRoller.stop();
        hp.set(0);
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
    public double getBarrier() { return barrier.get(); }
    public void setBarrier(double value) { barrier.set(value); }

    public DoubleProperty barrierProperty() { return barrier; }

    // === Armor ===
    public int getArmorAmount() { return armorAmount.get(); }
    public void setArmorAmount(int value) { armorAmount.set(value); }
    public IntegerProperty armorProperty() { return armorAmount; }
}
