package dev.dfbridge.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DFAnnounceEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String type;
    private final Map<String, String> data;

    public DFAnnounceEvent(String type, Map<String, String> data) {
        super(true); // async safe: fired from main thread typically; keeping true is fine for safety
        this.type = type;
        this.data = data == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(data));
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getData() {
        return data;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
