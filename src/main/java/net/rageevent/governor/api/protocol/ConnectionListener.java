package net.rageevent.governor.api.protocol;

import net.rageevent.governor.api.EnvelopeConnection;
import net.rageevent.governor.api.shipments.system.HandshakeRequest;

public interface ConnectionListener {

    default String handshake(EnvelopeConnection connection, HandshakeRequest handshake) {
        return null;
    }

    default void ready(EnvelopeConnection connection) {

    }

    default void rejected(EnvelopeConnection connection, String reason) {

    }

    default void disconnect(EnvelopeConnection connection) {

    }
}
