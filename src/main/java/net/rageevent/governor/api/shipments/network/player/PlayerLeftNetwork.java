package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;

import java.util.UUID;

public record PlayerLeftNetwork(UUID player) implements Envelope {
}
