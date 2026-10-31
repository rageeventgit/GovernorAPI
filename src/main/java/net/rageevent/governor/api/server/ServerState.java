package net.rageevent.governor.api.server;

public enum ServerState {

    OPEN,
    IN_GAME,
    CLOSED;

    public boolean joinable() {
        return this == OPEN;
    }
}
