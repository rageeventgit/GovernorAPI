package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;

import java.util.UUID;

public record FindPlayerResult(UUID player, String name, boolean online, String proxy, String server) implements Envelope {
}
