package fraymark.combat.damage.pipeline;

import fraymark.combat.damage.DamageContext;

/***
 * Interface for something that can process damage.
 */
public interface DamageHandler {
    boolean handle(DamageContext ctx);
}