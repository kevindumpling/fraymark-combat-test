package fraymark.model.actions.weaves;

/** Simple scaling: damage multiplier = 1 + min(capMul, perPointMul * trpSpent) */
public final class TrpScalingProfile {
    private final double perPointMul;  // e.g., 0.05 => +5% per TRP
    private final double capMul;       // e.g., 2.0 => up to +200%

    public TrpScalingProfile(double perPointMul, double capMul) {
        this.perPointMul = perPointMul;
        this.capMul = capMul;
    }
    public double perPointMul() { return perPointMul; }
    public double capMul() { return capMul; }
}
