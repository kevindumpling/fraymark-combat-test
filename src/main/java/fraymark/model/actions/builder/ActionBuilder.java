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
        List<String> fieldIds = this.parseFieldIds(data);  // calling on a helper for this one



        System.out.println("Building action: " + data.get("id") + " (" + type + ")");

        // === 2. BUILD THE ACTION INSTANCE ===
        Action action = switch (type.toUpperCase(Locale.ROOT)) {
            case "PHYSICAL_BASIC", "WEAPON" -> new BasicPhysicalAction(name, power, trpCost, mgGainOrCost, momentumProfile, closeRangeProfile, executionProfile,
                    flavorOnUse,
                    TargetingMode.valueOf(targeting), AttackRangeKind.valueOf(rangeKind), aoeDmgMul, aoeEffMul, aoeEffects);
            case "WEAVE" -> new WeaveAction(name, WeaveSchool.valueOf(schoolStr), power, trpCost, flavorOnUse, barrierIgnorePct,
                    resBypassPct, resBypassFlat, TrpSpendMode.valueOf(spendModeStr), trpScalingProfile,
                    TargetingMode.valueOf(targeting), AttackRangeKind.valueOf(rangeKind), aoeDmgMul, aoeEffMul, aoeEffects);
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };

        attachEffectsToAction(action, data);  // attatch any effects that the action may have.
        attatchFieldIdsToWeave(action, fieldIds);  // attatch any fields if the action is a weave.

        return action;
    }

    private void attatchFieldIdsToWeave(Action action, List<String> ids) {
        if (action instanceof WeaveAction){
            WeaveAction weave = (WeaveAction) action;
            for (String id : ids) {
                weave.addFieldId(id);
            }
        }
    }

    private List<String> parseFieldIds(Map<String, Object> data) {
        List<String> fieldIds = List.of();
        Object rawFields = data.get("fields");
        if (rawFields instanceof List<?> list) {
            List<String> tmp = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof String s && !s.isBlank()) {
                    tmp.add(s);
                }
            }
            fieldIds = List.copyOf(tmp);
        }

        return fieldIds;
    }

    private List<EffectDescriptor> parseAoeEffects(Map<String, Object> data) {
        List<EffectDescriptor> aoeEffects = new ArrayList<>();
        Object raw = data.get("aoeEffects");
        if (!(raw instanceof List<?> list)) return aoeEffects;

        for (Object o : list) {
            EffectDescriptor desc = null;

            if (o instanceof String id) {
                // ID-based AoE effect
                desc = effectFactory.descriptorFromId(id);

            } else if (o instanceof Map<?,?> mRaw) {
                Map<String, Object> m = (Map<String, Object>) mRaw;
                String id = (String) m.get("id");
                if (id != null) {
                    Map<String, Object> overrides = new HashMap<>(m);
                    overrides.remove("id");
                    desc = effectFactory.descriptorFromId(id, overrides);
                } else {
                    String t = (String) m.get("type");
                    if (t != null) {
                        // Legacy inline AoE effect definition
                        desc = effectFactory.descriptor(t, m);
                    } else {
                        System.err.println("AoE effect entry missing 'type' and 'id': " + m);
                    }
                }
            } else {
                System.err.println("Skipping aoeEffect entry (neither String nor Map): " + o);
            }

            if (desc != null) {
                aoeEffects.add(desc);
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

    @SuppressWarnings("unchecked")
    private void attachEffectsToAction(Action action, Map<String, Object> data) {
        Object rawEffects = data.get("effects");
        if (!(rawEffects instanceof List<?> list) || list.isEmpty()) return;

        List<EffectDescriptor> descriptors = new ArrayList<>();

        for (Object o : list) {
            EffectDescriptor desc = null;

            if (o instanceof String id) {
                // e.g. "burn"
                desc = effectFactory.descriptorFromId(id);

            } else if (o instanceof Map<?, ?> mRaw) {
                Map<String, Object> m = (Map<String, Object>) mRaw;

                // if an id is present, treat as overrides of a base template
                String id = (String) m.get("id");
                if (id != null) {
                    // remove 'id' from overrides so it doesn't mask the template id
                    Map<String, Object> overrides = new HashMap<>(m);
                    overrides.remove("id");
                    desc = effectFactory.descriptorFromId(id, overrides);
                } else {
                    // pure inline definition (legacy / special cases)
                    String type = (String) m.get("type");
                    if (type == null) {
                        System.err.println("Effect entry missing 'type': " + m);
                    } else {
                        desc = effectFactory.descriptor(type, m);
                    }
                }
            } else {
                System.err.println("Skipping effect entry (neither String nor Map): " + o);
            }

            if (desc != null) {
                descriptors.add(desc);
            }
        }

        if (action instanceof WeaveAction wa) {
            for (EffectDescriptor desc : descriptors) {
                wa.addEffectDescriptor(desc);
            }
        } else if (action instanceof BasicPhysicalAction pa) {
            for (EffectDescriptor desc : descriptors) {
                pa.addEffectDescriptor(desc);
            }
        } else {
            System.err.println("Action type lacks effectBundle adder: " + action.getClass());
        }
    }

    private static double toDouble(Object v, double d) {
        if (v instanceof Number n) return n.doubleValue();
        try { return v != null ? Double.parseDouble(v.toString()) : d; } catch(Exception e) { return d; }
    }
}



