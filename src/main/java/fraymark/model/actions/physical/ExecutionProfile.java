package fraymark.model.actions.physical;

public record ExecutionProfile(
        boolean enabled,         // must be true to attempt
        double thresholdPct,     // e.g., 0.10 for 10%
        boolean requiresNoArmor  // true: only if target has 0 armor
) {}
