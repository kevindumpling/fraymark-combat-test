package fraymark.model.actions.weaves;

import fraymark.model.actions.Action;
import fraymark.model.actions.AttackRangeKind;

public interface Weave extends Action {
    WeaveSchool getSchool();
    AttackRangeKind getRangeKind();         // keep parity with physicals
    double getBarrierIgnorePct();           // 0..1 (e.g., 0.25 ignores 25% barrier)
    double getResBypassPct();               // 0..1 (e.g., 0.30)
    int    getResBypassFlat();              // flat RES bypass

    int getTrpBaseCost();
    TrpSpendMode getTrpSpendMode();          // FLAT/VARIABLE/ALL
    TrpScalingProfile getTrpScalingProfile();
}
