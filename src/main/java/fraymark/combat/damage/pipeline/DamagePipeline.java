package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;
import java.util.ArrayList;
import java.util.List;

/***
 * A DamagePipeline is a complete damage resolver that uses individual handlers to go through
 * phases of the damage resolution process.
 */
public class DamagePipeline {
    private final List<DamageHandler> handlers = new ArrayList<>();

    public void addHandler(DamageHandler handler) { handlers.add(handler); }

    public void process(DamageContext ctx) {
        for (DamageHandler h : handlers) {
            if (h.handle(ctx)) break;
        }
    }
}