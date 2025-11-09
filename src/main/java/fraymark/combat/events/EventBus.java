package fraymark.combat.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Publish/Subscriber-based event bus. Effects, UI, and systems subscribe to receive relevant combat events.
 */
public class EventBus {
    private final List<EventListener> listeners = new ArrayList<>();

    public void subscribe(EventListener l) { listeners.add(l); }
    public void unsubscribe(EventListener l) { listeners.remove(l); }

    public void publish(CombatEvent e) {
        for (EventListener l : List.copyOf(listeners)) {
            l.onEvent(e);
        }
    }
}