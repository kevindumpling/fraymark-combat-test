package fraymark.model.actions.weaves;

public enum TrpSpendMode {
    FLAT,  // spends exact geTrpBaseCost()
    VARIABLE,  // spend X, scale damage by X, scale damage by TrpSpendProfile
    ALL  // dumps remaining TRP, handler scales by profile
}
