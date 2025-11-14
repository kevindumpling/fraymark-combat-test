package fraymark.model.actions.builder;

import fraymark.model.actions.*;
import fraymark.model.actions.physical.BasicPhysicalAction;
import fraymark.model.actions.physical.CloseRangeProfile;
import fraymark.model.actions.physical.ExecutionProfile;
import fraymark.model.actions.weaves.TrpScalingProfile;
import fraymark.model.actions.weaves.TrpSpendMode;
import fraymark.model.actions.weaves.WeaveAction;
import fraymark.model.actions.weaves.WeaveSchool;
import fraymark.model.effects.EffectDescriptor;
import fraymark.model.effects.factory.EffectFactory;

import java.util.*;
import fraymark.model.actions.physical.MomentumProfile;

public class ActionBuilder {
    private final EffectFactory effectFactory;

    public ActionBuilder(EffectFactory effectFactory) {this.effectFactory = effectFactory; }

    @SuppressWarnings("unchecked")
    public Action buildFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Missing 'type' field in action entry: " + data);
        }

        // === 1. BEGIN PARSING ACTION DATA ===
        String name = (String) data.getOrDefault("name", "Unnamed Action");
        int power = ((Number) data.getOrDefault("power", 0)).intValue();
        String schoolStr = (String) data.getOrDefault("school", "ALPHA");
        int trpCost = ((Number) data.getOrDefault("trpCost", 0)).intValue();  // this is a GAIN value for physicals
        double barrierIgnorePct = ((Number)data.getOrDefault("barrierIgnorePct", 0.0)).doubleValue();  // weave only
        double resBypassPct  = ((Number)data.getOrDefault("resBypassPct", 0.0)).doubleValue(); // weave only
        int    resBypassFlat = ((Number)data.getOrDefault("resBypassFlat", 0)).intValue(); // weave only
        String spendModeStr = (String) data.getOrDefault("trpSpendMode", "FLAT");
        TrpScalingProfile trpScalingProfile = this.parseTrpScalingProfile(data);

        int mgGainOrCost = ((Number) data.getOrDefault("mgGainOrCost", 0)).intValue();
        String flavorOnUse = (String) data.getOrDefault("flavorOnUse", "");
        MomentumProfile momentumProfile = this.parseMomentumProfile(data);
        CloseRangeProfile closeRangeProfile = this.parseCloseRangeProfile(data);
        ExecutionProfile executionProfile = this.parseExecutionProfile(data);

        String targeting = (String) data.getOrDefault("targeting", "SINGLE");
        String rangeKind = (String) data.getOrDefault("rangeKind", "ALL");
        double aoeDmgMul = ((Number) data.getOrDefault("aoeDamageMultiplier", 1.0)).doubleValue();
        double aoeEffMul = ((Number) data.getOrDefault("aoeEffectMultiplier", 1.0)).doubleValue();
        List<EffectDescriptor> aoeEffects = this.parseAoeEffects(data);  // calling on a helper for this one



        System.out.println("Building action: " + data.get("id") + " (" + type + ")");

        // === 2. BUILD THE ACTION INSTANCE ===
        Action action = switch (type.toUpperCase(Locale.ROOT)) {
            case "PHYSICAL_BASIC", "WEAPON" -> new BasicPhysicalAction(name, power, trpCost, mgGainOrCost, momentumProfile, closeRangeProfile, executionProfile,
                    flavorOnUse,
                    TargetingMode.valueOf(targeting), AttackRangeKind.valueOf(rangeKind), aoeDmgMul, aoeEffMul, aoeEffects);
            case "WEAVE" -> new WeaveAction(name, WeaveSchool.valueOf(schoolStr), power, trpCost, flavorOnUse,barrierIgnorePct,
                    resBypassPct, resBypassFlat, TrpSpendMode.valueOf(spendModeStr), trpScalingProfile,
                    TargetingMode.valueOf(targeting), AttackRangeKind.valueOf(rangeKind), aoeDmgMul, aoeEffMul, aoeEffects);
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };

        // Parse optional effects: a list of objects like
        //    { "type": "BLEED", "magnitude": 6, "duration": 2, "name": "Frostbite" }
        Object rawEffects = data.get("effects");
        if (rawEffects instanceof List<?> list && !list.isEmpty()) {
            for (Object o : list) {
                if (!(o instanceof Map)) {
                    System.err.println("Skipping effect entry (not an object): " + o);
                    continue;
                }
                Map<String, Object> eData = (Map<String, Object>) o;
                String eType = (String) eData.get("type");
                if (eType == null || eType.isBlank()) {
                    System.err.println("Skipping effect entry (missing 'type'): " + eData);
                    continue;
                }

                // create a descriptor with parameters (no src/tgt yet)
                EffectDescriptor desc = effectFactory.descriptor(eType, eData);

                // attach to the action (requires an adder on the concrete action)
                if (action instanceof WeaveAction wa) {
                    wa.addEffectDescriptor(desc);
                } else if (action instanceof BasicPhysicalAction pa) {
                    pa.addEffectDescriptor(desc);
                } else {
                    // If you add more Action types later, handle them here
                    System.err.println("Action type lacks effectBundle adder: " + action.getClass());
                }
            }
        }

        return action;
    }

    private List<EffectDescriptor>parseAoeEffects(Map<String, Object> data){
        List<EffectDescriptor> aoeEffects = new ArrayList<>();
        Object rawAoe = data.get("aoeEffects");
        if (rawAoe instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?,?> m) {
                    String t = (String) m.get("type");
                    if (t != null) {
                        aoeEffects.add(effectFactory.descriptor(t, (Map<String,Object>) m));
                    }
                }
            }
        }
        return aoeEffects;
    }

    private MomentumProfile parseMomentumProfile(Map<String, Object> data){
        MomentumProfile mp = null;
        var m = (Map<String,Object>) data.get("momentumProfile");
        if (m != null) {
            String curve = (String)m.getOrDefault("curve","linear");
            double per = ((Number)m.getOrDefault("perMg", 0.0)).doubleValue();
            double cap = ((Number)m.getOrDefault("cap", 0.0)).doubleValue();
            mp = new MomentumProfile(curve, per, cap);
            System.out.println("GWIMA BLAAAST" + mp.toString());

        }
        return mp;
    }

    private CloseRangeProfile parseCloseRangeProfile(Map<String, Object> data){
        CloseRangeProfile cr = null;
        var c = (Map<String,Object>) data.get("closeRangeProfile");
        if (c != null) {
            double dmgMul = ((Number)c.getOrDefault("damageMul",1.0)).doubleValue();
            double bypass = ((Number)c.getOrDefault("defBypassPct",0.0)).doubleValue();
            double mgMul = ((Number)c.getOrDefault("mgGainMul",1.0)).doubleValue();
            cr = new CloseRangeProfile(dmgMul, bypass, mgMul);
        }
        return cr;
    }

    private ExecutionProfile parseExecutionProfile(Map<String, Object> data){
        ExecutionProfile exec = null;
        var ex = (Map<String,Object>) data.get("executionProfile");
        if (ex != null) {
            boolean enabled = Boolean.TRUE.equals(ex.get("enabled"));
            double thr = ((Number)ex.getOrDefault("thresholdPct", 0.10)).doubleValue();
            boolean noArmor = Boolean.TRUE.equals(ex.get("requiresNoArmor"));
            exec = new ExecutionProfile(enabled, thr, noArmor);
        }

        return exec;

    }

    private TrpScalingProfile parseTrpScalingProfile(Map<String, Object> data){
        TrpScalingProfile scaleProfile = null;
        Object scaleObj = data.get("trpScalingProfile");
        if (scaleObj instanceof Map<?,?> m) {
            double per = toDouble(m.get("perPointMul"), 0.0);
            double cap = toDouble(m.get("capMul"), 0.0);

            // Optional min/max TRP spends for VARIABLE weaves.
            Object minObj = m.get("minExtra");
            Object maxObj = m.get("maxExtra");
            int min = (minObj instanceof Number n) ? n.intValue() : 0;
            int max = (maxObj instanceof Number n) ? n.intValue() : Integer.MAX_VALUE;

            scaleProfile = new TrpScalingProfile(per, cap, min, max);
        }
        return scaleProfile;
    }

    private static double toDouble(Object v, double d) {
        if (v instanceof Number n) return n.doubleValue();
        try { return v != null ? Double.parseDouble(v.toString()) : d; } catch(Exception e) { return d; }
    }
}



