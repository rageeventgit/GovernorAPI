package net.rageevent.governor.api.shipments.network.server;

import net.rageevent.governor.api.Envelope;
import net.rageevent.myriad.server.ServerType;

public record RemoveGameServer(String name) implements Envelope {
}
