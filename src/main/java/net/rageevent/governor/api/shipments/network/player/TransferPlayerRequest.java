package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.Transaction;

import java.util.UUID;

public record TransferPlayerRequest(UUID player, String server) implements Transaction<TransferResult> {
}
