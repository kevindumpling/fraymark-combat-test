// fraymark/model/actions/TargetingMode.java
package fraymark.model.actions;
public enum TargetingMode {
    SINGLE,             // current behavior
    AOE_TIER,           // hit every unit in the same tier as the clicked target (ally/enemy)
    AOE_ALL_TIER_SIDE,  // like AOE_TIER but only same side as target (ally-only or enemy-only)
    AOE_ALL             // (optional) hit both tiers on a side
}


