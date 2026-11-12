package net.rageevent.governor.api.shipments.network.server;


import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.server.GameMetaData;
import net.rageevent.governor.api.server.ServerProfile;

public record SupplyGameProfile(ServerProfile profile, GameMetaData metaData) implements Envelope {
}
