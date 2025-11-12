package fraymark.model.actions.physical;

public record CloseRangeProfile(
        double damageMul,       // e.g., 1.2
        double defBypassPct,    // e.g., 0.30  (30% of DEF ignored)
        double mgGainMul        // e.g., 1.5   (50% more MG on hit)
) {}