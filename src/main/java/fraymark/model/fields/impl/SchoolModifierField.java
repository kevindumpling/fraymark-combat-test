package fraymark.model.fields.impl;
import fraymark.combat.damage.DamageContext;
import fraymark.model.actions.weaves.WeaveAction;
import fraymark.model.fields.*;

import java.util.Map;

public class SchoolModifierField implements Field {
    private final String id;
    private final String name;
    private final String description;
    private final int duration;
    private final Map<String, Double> schoolMul;
    private final Map<String, Double> weaveMul;

    public SchoolModifierField(String id, String name, String description,
                               int duration,
                               Map<String, Double> schoolMul,
                               Map<String, Double> weaveMul) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.schoolMul = schoolMul;
        this.weaveMul = weaveMul;
    }

    @Override public String id() { return id; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public int initialDuration() { return duration; }

    @Override
    public void onDamage(DamageContext ctx) {
        if (ctx.action() instanceof WeaveAction wa) {
            // school boost
            double mul = schoolMul.getOrDefault(wa.getSchool().name(), 1.0);

            // specific weave boost
            String actionId = wa.getName(); // TODO: uses name instead of ID since WeaveAction doesn't know its ID
            mul *= weaveMul.getOrDefault(actionId, 1.0);

            ctx.setFinalDamage(ctx.finalDamage() * mul);
        }
    }
}


