package fraymark.combat.engine;

import fraymark.combat.damage.DamageContext;
import fraymark.combat.events.CombatEvent;
import fraymark.combat.events.CombatEventType;
import fraymark.combat.events.EventBus;
import fraymark.model.combatants.Combatant;
import fraymark.model.effects.ExpireReason;
import fraymark.model.fields.Field;
import fraymark.model.fields.FieldDescriptor;
import fraymark.model.fields.FieldInstance;

import java.util.*;

public class FieldManager {
    private final EventBus bus;
    private final List<FieldInstance> active = new ArrayList<>();

    public FieldManager(EventBus bus) {
        this.bus = bus;
        System.out.println("[FieldManager] created @" + System.identityHashCode(this));

    }

    /** Called by DataAssembler/scenario loader. */
    public void addField(FieldDescriptor desc, BattleState state) {
        System.out.println("[FieldManager] queueField "
                + " on manager @" + System.identityHashCode(this));

        Field f = desc.instantiate(state);
        FieldInstance inst = new FieldInstance(f);
        inst.applyIfNeeded(state);   // onApply called here, not pre-battle
        active.add(inst);
        System.out.println("fieldmanager has added to active: " + inst.getClass());    }


    public List<FieldInstance> getActive() {
        return Collections.unmodifiableList(active);
    }

    /** Called from BattleEngine.performAction at the start of a unit's turn. */
    public void onTurnStart(BattleState state, Combatant actor) {
        for (FieldInstance inst : active) {
            inst.field().onTurnStart(state, actor);
            System.out.println("[fieldmanager] field found in onturnstart");
        }
    }

    /** Damage-time hook. */
    public void onDamage(DamageContext ctx) {
        for (FieldInstance inst : active) {
            inst.field().onDamage(ctx);
        }
    }

    /**
     * End-of-round phase 1: let fields tick, generate AoE damage events.
     * (We’ll handle duration & expiry in a second phase to keep damage logs before fades.)
     */
    public List<CombatEvent> generateEndOfRoundEvents(BattleState state) {
        List<CombatEvent> out = new ArrayList<>();
        System.out.println("[FieldManager] generateEndOfRoundEvents active=" + active.size()
                + " @" + System.identityHashCode(this));
        // 1) per-round hooks (non-damage behaviours)
        for (FieldInstance inst : active) {
            inst.tickRound(state); // calls field.onRoundTick + decrements remaining
        }

        // 2) periodic effects + damage
        List<Combatant> all = new ArrayList<>(state.getParty());
        all.addAll(state.getEnemies());

        for (FieldInstance inst : active) {
            Field f = inst.field();

            // >>> COLLECT AuraField's EFFECT_APPLIED events <<<
            List<CombatEvent> evs = f.onTurnEnd(state);
            if (evs != null && !evs.isEmpty()) {
                out.addAll(evs);
            }

            // Any periodic damage the field wants to do
            for (Combatant target : all) {
                int dmg = f.periodicDamage(state, target);
                if (dmg > 0) {
                    String msg = f.periodicDamageMessage(state, target, dmg);
                    out.add(new CombatEvent(
                            CombatEventType.DAMAGE,
                            null,
                            target,
                            dmg,
                            msg
                    ));
                }
            }
        }

        return out;
    }

    /**
     * End-of-round phase 2: expire fields whose duration hit zero, and log fades.
     */
    public void finalizeEndOfRound(BattleState state) {
        var it = active.iterator();
        while (it.hasNext()) {
            FieldInstance inst = it.next();
            if (!inst.isInfinite() && inst.remaining() == 0) {
                inst.expire(state, bus, ExpireReason.NATURAL);
                it.remove();
            }
        }
    }

    public List<Field> getActiveFields() {
        List<Field> out = new ArrayList<>();
        for (FieldInstance inst : active) {
            out.add(inst.field());
        }
        return Collections.unmodifiableList(out);
    }
}

