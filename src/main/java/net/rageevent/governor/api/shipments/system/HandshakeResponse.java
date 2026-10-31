package net.rageevent.governor.api.shipments.system;

import net.rageevent.governor.api.Envelope;

public record HandshakeResponse(boolean accepted, String hub, String reason) implements Envelope {
    public static HandshakeResponse accepted(String hubName) {
        return new HandshakeResponse(true, hubName, null);
    }
    public static HandshakeResponse rejected(String reason) {
        return new HandshakeResponse(false, null, reason);
    }
}
