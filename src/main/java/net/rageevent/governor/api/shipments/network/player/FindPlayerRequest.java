package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Transaction;

public record FindPlayerRequest(String name) implements Transaction<FindPlayerResult> {
}
