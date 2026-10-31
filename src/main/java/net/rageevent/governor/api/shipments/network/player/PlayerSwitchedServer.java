package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;

import java.util.UUID;

public record PlayerSwitchedServer(UUID player, String server) implements Envelope {
}
