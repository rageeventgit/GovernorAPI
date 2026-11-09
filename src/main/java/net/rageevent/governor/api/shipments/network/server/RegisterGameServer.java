package net.rageevent.governor.api.shipments.network.server;

import net.rageevent.governor.api.Envelope;
import net.rageevent.myriad.api.server.ServerType;

public record RegisterGameServer(String name, ServerType serverType, String host, int port) implements Envelope {
}
