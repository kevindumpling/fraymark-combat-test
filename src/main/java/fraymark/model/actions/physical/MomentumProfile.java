package fraymark.model.actions.physical;

public record MomentumProfile(
        String curve,       // "linear" | "quadratic" | "logistic" (start with "linear")
        double perMg,       // e.g., 0.01  => +1% per MG
        double cap          // e.g., 2.0   => max x2 damage
) {
    public double multiplier(int mg) {
        double m = 1.0 + perMg * mg;
        return Math.min(m, cap <= 0 ? Double.MAX_VALUE : cap);
    }
}
