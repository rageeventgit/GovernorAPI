package net.rageevent.governor.api.shipments.system;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.server.ServerProfile;

public record HandshakeRequest(int protocolVersion, String name,
                               ServerProfile profile) implements Transaction<HandshakeResponse> {
}
