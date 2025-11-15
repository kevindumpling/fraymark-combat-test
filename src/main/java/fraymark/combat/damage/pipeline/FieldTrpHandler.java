package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.engine.FieldManager;
import fraymark.model.fields.Field;

public class FieldTrpHandler implements DamageHandler {
    private final FieldManager fields;

    public FieldTrpHandler(FieldManager fields) {
        this.fields = fields;
    }

    @Override
    public boolean handle(DamageContext ctx) {
        int baseGain = ctx.getBaseMgGain(); // or your TRP gain value
        int modified = baseGain;
        for (Field f : fields.getActiveFields()) {
            modified = f.modifyTrpGain(ctx, modified);
        }
        ctx.setFinalTrpGain(modified);
        return false;
    }
}
