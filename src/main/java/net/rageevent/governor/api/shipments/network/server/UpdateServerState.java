package net.rageevent.governor.api.shipments.network.server;

import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.server.ServerState;

public record UpdateServerState(ServerState state, int maxPlayers) implements Envelope {
}
