// fraymark/model/position/Formation.java
package fraymark.model.position;

import java.util.List;

public enum Formation {
    KNOT { // everyone CLOSE
        @Override public List<DistanceTier> assign(int size) {
            return java.util.Collections.nCopies(size, DistanceTier.CLOSE);
        }
    },
    SPIRAL { // example: 2 CLOSE then the rest BEHIND
        @Override public List<DistanceTier> assign(int size) {
            java.util.ArrayList<DistanceTier> tiers = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) tiers.add(i < 2 ? DistanceTier.CLOSE : DistanceTier.BEHIND);
            return tiers;
        }
    };
    public abstract List<DistanceTier> assign(int partySize);
}
