package fraymark.model.actions.physical;

import fraymark.model.actions.Action;

public interface Physical extends Action {
    int getMgGainOrCost();                          // base MG gain on hit (you said you have this)
    MomentumProfile getMomentumProfile();     // nullable
    CloseRangeProfile getCloseRangeProfile(); // nullable
}
