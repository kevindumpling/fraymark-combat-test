package fraymark.model.actions.weaves;

/** Simple scaling: damage multiplier = 1 + min(capMul, perPointMul * trpSpent) */
public final class TrpScalingProfile {
    private final double perPointMul;  // e.g., 0.05 => +5% per TRP
    private final double capMul;       // e.g., 2.0 => up to +200%

    // Bounds on extra TRP above the base cost
    private final int minExtra;
    private final int maxExtra;

    public TrpScalingProfile(double perPointMul, double capMul) {
        this(perPointMul, capMul, 0, Integer.MAX_VALUE);
    }

    // Overload for VARIABLE weaves
    public TrpScalingProfile(double perPointMul, double capMul, int minExtra, int maxExtra) {
        this.perPointMul = perPointMul;
        this.capMul = capMul;

        int min = Math.max(0, minExtra);
        int max = Math.max(min, maxExtra); // ensure max ≥ min

        this.minExtra = min;
        this.maxExtra = max;
    }

    public double perPointMul() { return perPointMul; }
    public double capMul() { return capMul; }

    /** Minimum extra TRP (beyond base) this profile wants to use, if affordable. */
    public int minExtra()       { return minExtra; }

    /** Maximum extra TRP this profile will ever consider. */
    public int maxExtra()       { return maxExtra; }
}
