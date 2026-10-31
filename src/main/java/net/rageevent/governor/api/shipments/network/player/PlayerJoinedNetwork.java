package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;

import java.util.UUID;

public record PlayerJoinedNetwork(UUID player, String name, String server) implements Envelope {
}
