package net.rageevent.governor.api.shipments.network.server;


import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.server.GameMetaData;

public record SupplyGameProfile(String name, GameMetaData metaData) implements Envelope {
}
