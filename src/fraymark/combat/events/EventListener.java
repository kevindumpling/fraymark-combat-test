package fraymark.combat.events;

/***
 * Listens for CombatEvents.
 */
public interface EventListener {
    void onEvent(CombatEvent event);
}