package fraymark.model.actions;

import fraymark.combat.events.CombatEvent;
import java.util.List;

/***
 * ActionResult contains a list of CombatEvents, which compose the outcome of an action.
 */
public record ActionResult(List<CombatEvent> events) {
}